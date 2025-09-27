package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardAuthorization;
import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.exception.BoardNotFoundException;
import io.github.ggabriel67.kanvas.exception.BoardRoleNotFoundException;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.BoardEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService Unit Tests")
class BoardMemberServiceTest
{
    @Mock private BoardMemberRepository memberRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private BoardEventProducer boardEventProducer;
    @Mock private BoardAuthorization boardAuthorization;

    @InjectMocks
    private BoardMemberService memberService;

    @Test
    void addBoardMember_shouldPersistBoardMember() {
        User user = User.builder().id(1).build();
        Board board = Board.builder().id(100).build();

        memberService.addBoardMember(board, user, any());

        ArgumentCaptor<BoardMember> memberCaptor = ArgumentCaptor.forClass(BoardMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        BoardMember savedMember = memberCaptor.getValue();

        assertThat(savedMember.getUser().equals(user));
        assertThat(savedMember.getBoard().equals(board));
    }

    @Nested
    class ChangeMemberRoleTests {
        @Test
        void shouldChangeRoleAndSendEvent() {
            BoardRoleChangeRequest request = new BoardRoleChangeRequest(10, 100, BoardRole.ADMIN);

            Board board = Board.builder().id(100).build();
            BoardMember member = BoardMember.builder().id(10).role(BoardRole.EDITOR).build();

            when(boardRepository.findById(request.boardId())).thenReturn(Optional.of(board));
            when(memberRepository.findById(request.targetMemberId())).thenReturn(Optional.of(member));

            memberService.changeMemberRole(request);

            assertThat(member.getRole()).isEqualTo(BoardRole.ADMIN);
            verify(memberRepository).save(member);

            verify(boardEventProducer).sendRoleChanged(argThat(event ->
                    event.memberId().equals(10) &&
                            event.role().equals(BoardRole.ADMIN.name())
            ));
        }

        @Test
        void shouldThrowException_WhenBoardNotFound() {
            BoardRoleChangeRequest request = new BoardRoleChangeRequest(10, 999, BoardRole.ADMIN);
            when(boardRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.changeMemberRole(request))
                    .isInstanceOf(BoardNotFoundException.class)
                    .hasMessageContaining("Board not found");

            verifyNoInteractions(memberRepository, boardEventProducer);
        }

        @Test
        void shouldThrowException_WhenMemberNotFound() {
            BoardRoleChangeRequest request = new BoardRoleChangeRequest(99, 100, BoardRole.ADMIN);
            when(boardRepository.findById(request.boardId())).thenReturn(Optional.of(Board.builder().id(123).build()));
            when(memberRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.changeMemberRole(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(boardRepository).findById(100);
            verifyNoMoreInteractions(boardRepository, memberRepository, boardEventProducer);
        }
    }

    @Nested
    class RemoveMemberTests {
        @Test
        void shouldRemoveMemberAndSendEvent() {
            Board board = Board.builder().id(100).name("Board").build();
            User user = User.builder().id(1).username("john").build();
            BoardMember member = BoardMember.builder().id(10).user(user).board(board).build();

            BoardMemberRemoveRequest request = new BoardMemberRemoveRequest(member.getId(), board.getId());

            when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            memberService.removeMember(request);

            verify(memberRepository).delete(member);

            verify(boardEventProducer).sendMemberRemoved(argThat(event ->
                    event.memberId().equals(10) &&
                            event.userId().equals(1) &&
                            event.boardId().equals(100) &&
                            event.boardName().equals("Board")
            ));
        }

        @Test
        void shouldThrowException_WhenMemberNotFound() {
            Board board = Board.builder().id(100).build();
            BoardMemberRemoveRequest request = new BoardMemberRemoveRequest(999, board.getId());
            when(memberRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.removeMember(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(memberRepository, never()).delete(any());
            verifyNoInteractions(boardEventProducer);
        }
    }

    @Nested
    class GetBoardRoleTests {

        @Test
        void shouldReturnRole_WhenUserIsMemberOfBoard() {
            Integer boardId = 100;
            Integer userId = 1;

            when(boardAuthorization.getCurrentUserId()).thenReturn(userId);
            when(memberRepository.findRoleByUserIdAndBoardId(userId, boardId))
                    .thenReturn(Optional.of(BoardRole.ADMIN));

            String role = memberService.getBoardRole(boardId);

            assertThat(role).isEqualTo(BoardRole.ADMIN.name());

            verify(boardAuthorization).getCurrentUserId();
            verify(memberRepository).findRoleByUserIdAndBoardId(userId, boardId);
        }

        @Test
        void shouldThrowException_WhenUserHasNoRoleInBoard() {
            Integer boardId = 100;
            Integer userId = 1;

            when(boardAuthorization.getCurrentUserId()).thenReturn(userId);
            when(memberRepository.findRoleByUserIdAndBoardId(userId, boardId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.getBoardRole(boardId))
                    .isInstanceOf(BoardRoleNotFoundException.class)
                    .hasMessageContaining("User has no role in this board");

            verify(boardAuthorization).getCurrentUserId();
            verify(memberRepository).findRoleByUserIdAndBoardId(userId, boardId);
        }
    }
}
