package io.github.ggabriel67.kanvas.board.invitation;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardService;
import io.github.ggabriel67.kanvas.board.member.BoardMember;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.board.member.BoardMemberService;
import io.github.ggabriel67.kanvas.exception.*;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.kafka.producer.InvitationEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardInvitationService Unit Tests")
class BoardInvitationServiceTest
{
    @Mock private UserService userService;
    @Mock private BoardMemberService memberService;
    @Mock private BoardMemberRepository memberRepository;
    @Mock private BoardInvitationRepository invitationRepository;
    @Mock private BoardService boardService;
    @Mock private InvitationEventProducer invitationEventProducer;

    @InjectMocks
    private BoardInvitationService invitationService;

    @Nested
    class CreateInvitationTests {
        @Test
        void shouldThrowException_WhenUserIsAlreadyMember() {
            BoardInvitationRequest request = new BoardInvitationRequest(1, 2, 10, BoardRole.EDITOR);

            when(memberRepository.findByUserIdAndBoardId(2, 10))
                    .thenReturn(Optional.of(new BoardMember()));

            assertThatThrownBy(() -> invitationService.createInvitation(request))
                    .isInstanceOf(MemberAlreadyExistsException.class)
                    .hasMessageContaining("This user is already a member of the board");

            verify(invitationRepository, never()).save(any());
            verify(invitationEventProducer, never()).sendInvitationCreated(any());
        }

        @Test
        void shouldThrowException_WhenPendingInvitationAlreadyExists() {
            BoardInvitationRequest request = new BoardInvitationRequest(1, 2, 10, BoardRole.EDITOR);

            when(memberRepository.findByUserIdAndBoardId(2, 10)).thenReturn(Optional.empty());

            BoardInvitation existing = BoardInvitation.builder()
                    .status(InvitationStatus.PENDING)
                    .build();
            when(invitationRepository.findByInviteeIdAndBoardId(2, 10))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> invitationService.createInvitation(request))
                    .isInstanceOf(InvitationPendingException.class)
                    .hasMessageContaining("Invitation already pending");

            verify(invitationRepository, never()).save(any());
            verify(invitationEventProducer, never()).sendInvitationCreated(any());
        }

        @Test
        void shouldSaveInvitationAndSendKafkaEvent_WhenValidRequest() {
            BoardInvitationRequest request = new BoardInvitationRequest(1, 2, 10, BoardRole.EDITOR);

            when(memberRepository.findByUserIdAndBoardId(2, 10)).thenReturn(Optional.empty());
            when(invitationRepository.findByInviteeIdAndBoardId(2, 10)).thenReturn(Optional.empty());

            User inviter = User.builder().id(1).username("john").build();
            User invitee = User.builder().id(2).username("bob").build();
            Board board = Board.builder().id(10).name("Board").build();

            when(userService.getUserById(1)).thenReturn(inviter);
            when(userService.getUserById(2)).thenReturn(invitee);
            when(boardService.getBoardById(10)).thenReturn(board);

            BoardInvitation savedInvitation = BoardInvitation.builder()
                    .id(99)
                    .inviter(inviter)
                    .invitee(invitee)
                    .board(board)
                    .status(InvitationStatus.PENDING)
                    .role(BoardRole.EDITOR)
                    .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                    .build();
            when(invitationRepository.save(any())).thenReturn(savedInvitation);

            invitationService.createInvitation(request);

            verify(invitationRepository).save(any(BoardInvitation.class));
            verify(invitationEventProducer).sendInvitationCreated(argThat(event ->
                    event.invitationId().equals(99) &&
                            event.inviteeId().equals(2) &&
                            event.inviterUsername().equals("john") &&
                            event.targetName().equals("Board")
            ));
        }
    }

    @Nested
    class AcceptInvitationTests {
        @Test
        void shouldThrowException_WhenInvitationDoesNotExist() {
            when(invitationRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invitationService.acceptInvitation(99))
                    .isInstanceOf(InvitationNotFoundException.class)
                    .hasMessageContaining("Invitation does not exist");

            verifyNoInteractions(memberService, invitationEventProducer);
        }

        @Test
        void shouldAcceptInvitationAndSendKafkaEvent_WhenValid() {
            User invitee = User.builder().id(5).username("bob").build();
            Board board = Board.builder().id(42).name("Board").build();

            BoardInvitation invitation = BoardInvitation.builder()
                    .id(100)
                    .invitee(invitee)
                    .board(board)
                    .role(BoardRole.EDITOR)
                    .status(InvitationStatus.PENDING)
                    .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                    .build();

            when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

            invitationService.acceptInvitation(100);

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

            verify(invitationRepository).save(invitation);
            verify(memberService).addBoardMember(board, invitee, BoardRole.EDITOR);
            verify(invitationEventProducer).sendInvitationUpdated(argThat(event ->
                    event.invitationId().equals(100) &&
                            event.inviteeId().equals(5) &&
                            event.status().equals(InvitationStatus.ACCEPTED.name())
            ));
        }
    }

    @Nested
    class DeclineInvitationTests {
        @Test
        void shouldThrowException_WhenInvitationDoesNotExist() {
            when(invitationRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invitationService.declineInvitation(99))
                    .isInstanceOf(InvitationNotFoundException.class)
                    .hasMessageContaining("Invitation does not exist");

            verifyNoInteractions(memberService, invitationEventProducer);
        }

        @Test
        void shouldDeclineInvitationAndSendKafkaEvent_WhenValid() {
            User invitee = User.builder().id(5).username("bob").build();
            Board board = Board.builder().id(42).name("Board").build();

            BoardInvitation invitation = BoardInvitation.builder()
                    .id(100)
                    .invitee(invitee)
                    .board(board)
                    .role(BoardRole.EDITOR)
                    .status(InvitationStatus.PENDING)
                    .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                    .build();

            when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

            invitationService.declineInvitation(100);

            assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.DECLINED);

            verify(invitationRepository).save(invitation);
            verify(invitationEventProducer).sendInvitationUpdated(argThat(event ->
                    event.invitationId().equals(100) &&
                            event.inviteeId().equals(5) &&
                            event.status().equals(InvitationStatus.DECLINED.name())
            ));
        }
    }

    @Test
    void shouldThrowExceptionAndSendKafkaEvent_WhenInvitationIsExpired() {
        User invitee = User.builder().id(5).username("bob").build();
        Board board = Board.builder().id(42).name("Board").build();

        BoardInvitation invitation = BoardInvitation.builder()
                .id(100)
                .invitee(invitee)
                .board(board)
                .role(BoardRole.EDITOR)
                .status(InvitationStatus.PENDING)
                .expirationTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(100))
                .isInstanceOf(InvitationExpiredException.class)
                .hasMessageContaining("Invitation has expired");

        verify(invitationEventProducer).sendInvitationUpdated(argThat(event ->
                event.invitationId().equals(100) &&
                        event.inviteeId().equals(5) &&
                        event.status().equals(InvitationStatus.EXPIRED.name())
        ));
    }

    @Test
    void shouldThrowException_WhenInvitationIsNotPending() {
        User invitee = User.builder().id(5).username("bob").build();
        Board board = Board.builder().id(42).name("Board").build();

        BoardInvitation invitation = BoardInvitation.builder()
                .id(100)
                .invitee(invitee)
                .board(board)
                .role(BoardRole.EDITOR)
                .status(InvitationStatus.DECLINED)
                .expirationTime(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(invitationRepository.findById(100)).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(100))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("Invitation is not pending");
    }
}
