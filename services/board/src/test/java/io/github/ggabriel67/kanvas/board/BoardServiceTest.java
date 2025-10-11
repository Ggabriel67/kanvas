package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.authorization.board.BoardAuthorization;
import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.invitation.BoardInvitationRepository;
import io.github.ggabriel67.kanvas.board.member.BoardMember;
import io.github.ggabriel67.kanvas.board.member.BoardMemberMapper;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.exception.BoardNotFoundException;
import io.github.ggabriel67.kanvas.exception.NameAlreadyInUseException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.feign.TaskClient;
import io.github.ggabriel67.kanvas.kafka.producer.BoardEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService Unit Tests")
class BoardServiceTest
{
    @Mock private BoardRepository boardRepository;
    @Mock private UserService userService;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private BoardMemberRepository boardMemberRepository;
    @Mock private BoardMemberMapper boardMemberMapper;
    @Mock private BoardAuthorization boardAuth;
    @Mock private BoardInvitationRepository boardInvitationRepository;
    @Mock private BoardEventProducer boardEventProducer;
    @Mock private TaskClient taskClient;

    @InjectMocks
    private BoardService boardService;

    @Nested
    class CreateBoardTests {
        private User creator;
        private BoardRequest request;
        private Workspace workspace;

        @BeforeEach
        void setUp() {
            creator = User.builder().id(1).build();
            request = new BoardRequest(creator.getId(), 100, "My Board", "Description", BoardVisibility.WORKSPACE_PUBLIC);
            workspace = Workspace.builder().id(100).build();
        }

        @Test
        void shouldCreateBoardAndAssignCreatorAsAdmin() {
            when(userService.getUserById(1)).thenReturn(creator);
            when(workspaceRepository.findById(100)).thenReturn(Optional.of(workspace));
            when(boardRepository.findByNameAndWorkspace("My Board", workspace))
                    .thenReturn(Optional.empty());

            Board savedBoard = Board.builder().id(200).workspace(workspace).name("My Board").build();
            when(boardRepository.save(any(Board.class))).thenReturn(savedBoard);

            Integer boardId = boardService.createBoard(request);

            assertThat(boardId).isEqualTo(200);

            ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
            verify(boardRepository).save(boardCaptor.capture());
            assertThat(boardCaptor.getValue().getName()).isEqualTo("My Board");
            assertThat(boardCaptor.getValue().getWorkspace()).isEqualTo(workspace);

            ArgumentCaptor<BoardMember> memberCaptor = ArgumentCaptor.forClass(BoardMember.class);
            verify(boardMemberRepository).save(memberCaptor.capture());
            BoardMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getUser()).isEqualTo(creator);
            assertThat(savedMember.getBoard()).isEqualTo(savedBoard);
            assertThat(savedMember.getRole()).isEqualTo(BoardRole.ADMIN);
        }

        @Test
        void shouldThrowException_WhenWorkspaceDoesNotExist() {
            when(userService.getUserById(1)).thenReturn(creator);
            when(workspaceRepository.findById(100)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardService.createBoard(request))
                    .isInstanceOf(WorkspaceNotFoundException.class)
                    .hasMessageContaining("Workspace not found");

            verify(boardRepository, never()).save(any());
            verify(boardMemberRepository, never()).save(any());
        }

        @Test
        void shouldThrowException_WhenBoardNameAlreadyExists() {
            BoardRequest invalidRequest = new BoardRequest(1, 100, "Duplicate", "desc", BoardVisibility.WORKSPACE_PUBLIC);
            when(userService.getUserById(1)).thenReturn(creator);
            when(workspaceRepository.findById(100)).thenReturn(Optional.of(workspace));
            when(boardRepository.findByNameAndWorkspace("Duplicate", workspace))
                    .thenReturn(Optional.of(Board.builder().id(200).build()));

            assertThatThrownBy(() -> boardService.createBoard(invalidRequest))
                    .isInstanceOf(NameAlreadyInUseException.class)
                    .hasMessageContaining("Board with name Duplicate already exists in this workspace");

            verify(boardRepository, never()).save(any());
            verify(boardMemberRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateBoardTests {
        @Test
        void shouldUpdateBoard_WhenBoardExists() {
            Workspace workspace = Workspace.builder().id(100).build();
            Board existing = Board.builder().id(200).workspace(workspace).name("Old Name").description("Old Description").visibility(BoardVisibility.WORKSPACE_PUBLIC).build();

            BoardUpdateRequest updateRequest = new BoardUpdateRequest("New Name", "New Description", BoardVisibility.PRIVATE);

            when(boardRepository.findById(100)).thenReturn(Optional.of(existing));

            boardService.updateBoard(updateRequest, 100);

            assertThat(existing.getName()).isEqualTo("New Name");
            assertThat(existing.getDescription()).isEqualTo("New Description");
            assertThat(existing.getVisibility().equals(BoardVisibility.PRIVATE));
            verify(boardRepository).save(existing);
        }

        @Test
        void shouldThrowException_WhenBoardNotFound() {
            BoardUpdateRequest invalidRequest = new BoardUpdateRequest("Duplicate", "desc", BoardVisibility.WORKSPACE_PUBLIC);
            when(boardRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardService.updateBoard(invalidRequest, 999))
                    .isInstanceOf(BoardNotFoundException.class)
                    .hasMessageContaining("Board not found");

            verify(boardRepository, never()).save(any());
        }
    }

    @Nested
    class GetBoardByIdTests {
        @Test
        void shouldReturnBoard() {
            Board existingBoard = Board.builder().id(100).build();
            when(boardRepository.findById(100))
                    .thenReturn(Optional.of(existingBoard));

            assertThat(boardService.getBoardById(100)).isEqualTo(existingBoard);
        }

        @Test
        void shouldThrowException_WhenWorkspaceDoesNotExist() {
            when(boardRepository.findById(999))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardService.getBoardById(999))
                    .isInstanceOf(BoardNotFoundException.class)
                    .hasMessageContaining("Board not found");
        }
    }

    @Nested
    class GetBoardByWorkspaceTests {
        @Test
        void shouldReturnBoards_WhenWorkspaceExists() {
            Workspace workspace = Workspace.builder().id(100).build();

            List<BoardDtoProjection> boards = List.of(
                    new BoardDtoProjection(1, "Board 1"),
                    new BoardDtoProjection(2, "Board 2")
            );

            when(boardRepository.findByWorkspace(workspace)).thenReturn(boards);

            List<BoardDtoProjection> result = boardService.getAllBoardsByWorkspace(workspace);

            assertThat(result.size() == 2);
            assertThat(result.containsAll(boards));
            verify(boardRepository).findByWorkspace(workspace);
        }

        @Test
        void shouldReturnEmptyList_WhenNoBoardsExist() {
            Workspace workspace = Workspace.builder().id(200).build();

            when(boardRepository.findByWorkspace(workspace)).thenReturn(Collections.emptyList());

            List<BoardDtoProjection> result = boardService.getAllBoardsByWorkspace(workspace);

            assertThat(result.isEmpty());
            verify(boardRepository).findByWorkspace(workspace);
        }
    }

    @Nested
    class GetVisibleBoardsForMemberTests {
        @Test
        void shouldReturnVisibleBoardsForMember() {
            Integer userId = 1;
            Workspace workspace = Workspace.builder().id(100).build();

            List<BoardDtoProjection> visibleBoards = List.of(
                    new BoardDtoProjection(200, "Public Board 1"),
                    new BoardDtoProjection(201, "Public Board 2")
            );

            when(boardRepository.findBoardsForMemberByVisibility(userId, workspace, BoardVisibility.WORKSPACE_PUBLIC))
                    .thenReturn(visibleBoards);

            List<BoardDtoProjection> result = boardService.getPublicBoardsByWorkspaceAndMember(userId, workspace);

            assertThat(result.size() == 2);
            assertThat(result.containsAll(visibleBoards));

            verify(boardRepository).findBoardsForMemberByVisibility(userId, workspace, BoardVisibility.WORKSPACE_PUBLIC);
        }

        @Test
        void shouldReturnEmptyList_WhenNoVisibleBoardsExist() {
            Integer userId = 1;
            Workspace workspace = Workspace.builder().id(100).build();

            when(boardRepository.findBoardsForMemberByVisibility(userId, workspace, BoardVisibility.WORKSPACE_PUBLIC))
                    .thenReturn(List.of());

            List<BoardDtoProjection> result = boardService.getPublicBoardsByWorkspaceAndMember(userId, workspace);

            assertThat(result.isEmpty());
            verify(boardRepository).findBoardsForMemberByVisibility(userId, workspace, BoardVisibility.WORKSPACE_PUBLIC);
        }
    }

    @Nested
    class DeleteBoardTests {

        @Test
        void shouldDeleteBoardAndSendEvent() {
            Integer deletedBoardId = 123;
            Board board = Board.builder().id(deletedBoardId).build();

            when(boardRepository.findById(deletedBoardId)).thenReturn(Optional.of(board));

            boardService.deleteBoard(deletedBoardId);

            verify(boardMemberRepository).deleteAllByBoard(board);
            verify(boardInvitationRepository).deleteAllByBoard(board);
            verify(boardRepository).delete(board);

            verify(boardEventProducer).sendBoardDeleted(argThat(event ->
                    event.boardId().equals(deletedBoardId)
            ));
        }

        @Test
        void shouldThrowException_WhenBoardDoesNotExist() {
            Integer boardId = 999;
            when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> boardService.deleteBoard(boardId))
                    .isInstanceOf(BoardNotFoundException.class)
                    .hasMessageContaining("Board not found");

            verifyNoInteractions(boardMemberRepository, boardInvitationRepository, boardEventProducer);
            verify(boardRepository, never()).delete(any());
        }
    }
}
