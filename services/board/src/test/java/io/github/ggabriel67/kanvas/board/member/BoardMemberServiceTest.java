package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardAuthorization;
import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.board.BoardVisibility;
import io.github.ggabriel67.kanvas.board.invitation.BoardInvitationRepository;
import io.github.ggabriel67.kanvas.exception.BoardNotFoundException;
import io.github.ggabriel67.kanvas.exception.BoardRoleNotFoundException;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.BoardEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService Unit Tests")
class BoardMemberServiceTest
{
    @Mock private BoardMemberRepository boardMemberRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private BoardEventProducer boardEventProducer;
    @Mock private BoardAuthorization boardAuthorization;
    @Mock private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock private BoardInvitationRepository boardInvitationRepository;

    @InjectMocks
    private BoardMemberService memberService;

    @Test
    void addBoardMember_shouldPersistBoardMember() {
        User user = User.builder().id(1).firstname("Alice").lastname("Doe").username("alice").avatarColor("#abc").build();

        Board board = Board.builder().id(100).build();

        BoardRole boardRole = BoardRole.EDITOR;

        BoardMember savedMember = BoardMember.builder().id(50).user(user).board(board).role(boardRole).joinedAt(LocalDateTime.now()).build();

        when(boardMemberRepository.save(any(BoardMember.class))).thenReturn(savedMember);

        memberService.addBoardMember(board, user, boardRole);

        ArgumentCaptor<BoardMember> memberCaptor = ArgumentCaptor.forClass(BoardMember.class);
        verify(boardMemberRepository).save(memberCaptor.capture());
        BoardMember captured = memberCaptor.getValue();

        assertThat(captured.getUser()).isEqualTo(user);
        assertThat(captured.getBoard()).isEqualTo(board);
        assertThat(captured.getRole()).isEqualTo(boardRole);

        verify(boardEventProducer).sendMemberJoined(argThat(event ->
                event.boardId().equals(board.getId()) &&
                        event.memberId().equals(savedMember.getId()) &&
                        event.userId().equals(user.getId()) &&
                        event.boardRole().equals(boardRole.name())
        ));
    }

    @Nested
    class ChangeMemberRoleTests {
        @Test
        void shouldChangeRoleAndSendEvent() {
            BoardRoleChangeRequest request = new BoardRoleChangeRequest(10, 100, BoardRole.ADMIN);

            Board board = Board.builder().id(100).build();
            BoardMember member = BoardMember.builder().id(10).board(board).role(BoardRole.EDITOR).build();

            when(boardRepository.findById(request.boardId())).thenReturn(Optional.of(board));
            when(boardMemberRepository.findById(request.targetMemberId())).thenReturn(Optional.of(member));

            memberService.changeMemberRole(request);

            assertThat(member.getRole()).isEqualTo(BoardRole.ADMIN);
            verify(boardMemberRepository).save(member);

            verify(boardEventProducer).sendRoleChanged(argThat(event ->
                    event.boardId().equals(board.getId()) &&
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

            verifyNoInteractions(boardMemberRepository, boardEventProducer);
        }

        @Test
        void shouldThrowException_WhenMemberNotFound() {
            BoardRoleChangeRequest request = new BoardRoleChangeRequest(99, 100, BoardRole.ADMIN);
            when(boardRepository.findById(request.boardId())).thenReturn(Optional.of(Board.builder().id(123).build()));
            when(boardMemberRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.changeMemberRole(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(boardRepository).findById(100);
            verifyNoMoreInteractions(boardRepository, boardMemberRepository, boardEventProducer);
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

            when(boardRepository.findById(request.boardId())).thenReturn(Optional.of(board));
            when(boardMemberRepository.findById(member.getId())).thenReturn(Optional.of(member));

            memberService.removeMember(request);

            verify(boardMemberRepository).delete(member);
            verify(boardInvitationRepository).deleteAllByInvitee(member.getUser());
            verify(boardEventProducer).sendMemberRemoved(argThat(event ->
                    event.memberId().equals(10) &&
                            event.userId().equals(1) &&
                            event.boardId().equals(100) &&
                            event.boardName().equals("Board")
            ));
        }

        @Test
        void shouldThrowException_WhenBoardNotFound() {
            BoardMemberRemoveRequest request = new BoardMemberRemoveRequest(10, 999);
            when(boardRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.removeMember(request))
                    .isInstanceOf(BoardNotFoundException.class)
                    .hasMessageContaining("Board not found");

            verifyNoInteractions(boardMemberRepository, boardEventProducer);
        }

        @Test
        void shouldThrowException_WhenMemberNotFound() {
            Board board = Board.builder().id(100).build();
            BoardMemberRemoveRequest request = new BoardMemberRemoveRequest(999, board.getId());

            when(boardRepository.findById(request.boardId())).thenReturn(Optional.of(board));
            when(boardMemberRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.removeMember(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(boardMemberRepository, never()).delete(any());
            verifyNoInteractions(boardEventProducer);
        }
    }

    @Nested
    class GetBoardRoleTests {

        private final Integer boardId = 100;
        private final Integer userId = 1;

        @Test
        void shouldReturnRole_WhenUserIsMemberOfBoard() {
            when(boardAuthorization.getCurrentUserId()).thenReturn(userId);
            when(boardMemberRepository.findRoleByUserIdAndBoardId(userId, boardId))
                    .thenReturn(Optional.of(BoardRole.ADMIN));

            String role = memberService.getBoardRole(boardId);

            assertThat(role).isEqualTo(BoardRole.ADMIN.name());

            verify(boardAuthorization).getCurrentUserId();
            verify(boardMemberRepository).findRoleByUserIdAndBoardId(userId, boardId);
            verifyNoInteractions(boardRepository, workspaceMemberRepository);
        }

        @Test
        void shouldReturnViewer_WhenUserCanViewWorkspacePublicBoard() {
            Workspace workspace = Workspace.builder().id(77).build();
            Board board = Board.builder().id(boardId).workspace(workspace).visibility(BoardVisibility.WORKSPACE_PUBLIC).build();
            User user = User.builder().id(1).build();

            WorkspaceMember workspaceMember = WorkspaceMember.builder().id(99).user(user).workspace(workspace).build();

            when(boardAuthorization.getCurrentUserId()).thenReturn(userId);
            when(boardMemberRepository.findRoleByUserIdAndBoardId(userId, boardId))
                    .thenReturn(Optional.empty());
            when(boardRepository.findById(boardId))
                    .thenReturn(Optional.of(board));
            when(workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspace.getId()))
                    .thenReturn(Optional.of(workspaceMember));

            String role = memberService.getBoardRole(boardId);

            assertThat(role).isEqualTo(BoardRole.VIEWER.name());

            verify(boardAuthorization).getCurrentUserId();
            verify(boardMemberRepository).findRoleByUserIdAndBoardId(userId, boardId);
            verify(boardRepository).findById(boardId);
            verify(workspaceMemberRepository).findByUserIdAndWorkspaceId(userId, workspace.getId());
        }

        @Test
        void shouldThrowException_WhenUserHasNoAccessToBoard() {
            Workspace workspace = Workspace.builder().id(77).build();
            Board board = Board.builder()
                    .id(boardId)
                    .workspace(workspace)
                    .visibility(BoardVisibility.PRIVATE)
                    .build();

            when(boardAuthorization.getCurrentUserId()).thenReturn(userId);
            when(boardMemberRepository.findRoleByUserIdAndBoardId(userId, boardId))
                    .thenReturn(Optional.empty());
            when(boardRepository.findById(boardId))
                    .thenReturn(Optional.of(board));
            when(workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspace.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.getBoardRole(boardId))
                    .isInstanceOf(BoardRoleNotFoundException.class)
                    .hasMessageContaining("User has no permissions in this board");

            verify(boardAuthorization).getCurrentUserId();
            verify(boardMemberRepository).findRoleByUserIdAndBoardId(userId, boardId);
            verify(boardRepository).findById(boardId);
            verify(workspaceMemberRepository).findByUserIdAndWorkspaceId(userId, workspace.getId());
        }
    }
}
