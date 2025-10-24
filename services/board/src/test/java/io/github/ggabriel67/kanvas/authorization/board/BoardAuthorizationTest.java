package io.github.ggabriel67.kanvas.authorization.board;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.board.BoardVisibility;
import io.github.ggabriel67.kanvas.board.member.BoardMember;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardAuthorization Unit Tests")
class BoardAuthorizationTest
{
    @Mock private BoardMemberRepository boardMemberRepository;
    @Mock private BoardRepository boardRepository;
    @Mock private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private BoardAuthorization boardAuth;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContextWithUserId(Integer userId) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userId);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void getCurrentUserId_shouldReturnUserId() {
        mockSecurityContextWithUserId(42);

        assertThat(boardAuth.getCurrentUserId()).isEqualTo(42);
    }

    @Test
    void getCurrentUserId_shouldThrow_WhenNoUser() {
        assertThatThrownBy(() -> boardAuth.getCurrentUserId())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No user ID in SecurityContext");
    }

    @Test
    void hasRole_shouldReturnCorrectBooleans() {
        Integer boardId = 1;
        Integer userId = 42;

        BoardMember adminMember = new BoardMember();
        adminMember.setRole(BoardRole.ADMIN);

        BoardMember editorMember = new BoardMember();
        editorMember.setRole(BoardRole.EDITOR);

        BoardMember viewerMember = new BoardMember();
        viewerMember.setRole(BoardRole.VIEWER);

        when(boardMemberRepository.findByUserIdAndBoardId(userId, boardId))
                .thenReturn(Optional.of(adminMember));
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.ADMIN)).isTrue();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.EDITOR)).isTrue();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.VIEWER)).isTrue();

        when(boardMemberRepository.findByUserIdAndBoardId(userId, boardId))
                .thenReturn(Optional.of(editorMember));
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.ADMIN)).isFalse();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.EDITOR)).isTrue();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.VIEWER)).isTrue();

        when(boardMemberRepository.findByUserIdAndBoardId(userId, boardId))
                .thenReturn(Optional.of(viewerMember));
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.ADMIN)).isFalse();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.EDITOR)).isFalse();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.VIEWER)).isTrue();

        when(boardMemberRepository.findByUserIdAndBoardId(userId, boardId))
                .thenReturn(Optional.empty());
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.ADMIN)).isFalse();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.EDITOR)).isFalse();
        assertThat(boardAuth.hasRole(userId, boardId, BoardRole.VIEWER)).isFalse();
    }

    @Test
    void isAdmin_shouldReturnTrue_WhenUserIsAdmin() {
        mockSecurityContextWithUserId(42);

        when(boardMemberRepository.findByUserIdAndBoardId(42, 1))
                .thenReturn(Optional.of(new BoardMember(){{
                    setRole(BoardRole.ADMIN);
                }}));
        assertThat(boardAuth.isAdmin(1)).isTrue();
    }

    @Test
    void isMember_shouldReturnTrue_WhenUserIsMember() {
        mockSecurityContextWithUserId(42);

        when(boardMemberRepository.findByUserIdAndBoardId(42, 1))
                .thenReturn(Optional.of(new BoardMember(){{
                    setRole(BoardRole.EDITOR);
                }}));
        assertThat(boardAuth.isMember(1)).isTrue();
    }

    @Test
    void canView_shouldReturnTrue_ForWorkspaceMemberAndPublicBoard() {
        mockSecurityContextWithUserId(42);

        Workspace workspace = Workspace.builder().id(100).build();
        Board board = Board.builder().id(1).workspace(workspace).visibility(BoardVisibility.WORKSPACE_PUBLIC).build();

        when(boardRepository.findById(1)).thenReturn(Optional.of(board));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(42, 100))
                .thenReturn(Optional.of(new WorkspaceMember()));

        assertThat(boardAuth.canView(1)).isTrue();
    }

    @Test
    void canView_shouldReturnFalse_WhenNotMemberAndPrivateBoard() {
        mockSecurityContextWithUserId(42);

        Workspace workspace = Workspace.builder().id(100).build();
        Board board = Board.builder().id(1).workspace(workspace).visibility(BoardVisibility.PRIVATE).build();

        when(boardRepository.findById(1)).thenReturn(Optional.of(board));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(42, 100))
                .thenReturn(Optional.empty());

        assertThat(boardAuth.canView(1)).isFalse();
    }

    @Test
    void canModerate_shouldReturnTrue_WhenCallerIsOwner() {
        mockSecurityContextWithUserId(1);

        Workspace workspace = Workspace.builder().id(10).build();
        Board board = Board.builder().id(100).workspace(workspace).build();

        when(boardRepository.findById(100)).thenReturn(Optional.of(board));
        when(boardMemberRepository.findByUserIdAndBoardId(1, 100))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.EDITOR).build()));
        when(boardMemberRepository.findById(2))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.ADMIN).build()));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 10))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.OWNER).build()));

        assertThat(boardAuth.canModerate(100, 2)).isTrue();
    }

    @Test
    void canModerate_shouldReturnTrue_WhenCallerIsAdminAndTargetIsEditorOrViewer() {
        mockSecurityContextWithUserId(1);

        Workspace workspace = Workspace.builder().id(10).build();
        Board board = Board.builder().id(100).workspace(workspace).build();

        when(boardRepository.findById(100)).thenReturn(Optional.of(board));
        when(boardMemberRepository.findByUserIdAndBoardId(1, 100))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.ADMIN).build()));
        when(boardMemberRepository.findById(2))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.EDITOR).build()));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 10))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build()));

        assertThat(boardAuth.canModerate(100, 2)).isTrue();
    }

    @Test
    void canModerate_shouldReturnFalse_WhenCallerIsEditor() {
        mockSecurityContextWithUserId(1);

        Workspace workspace = Workspace.builder().id(10).build();
        Board board = Board.builder().id(100).workspace(workspace).build();

        when(boardRepository.findById(100)).thenReturn(Optional.of(board));
        when(boardMemberRepository.findByUserIdAndBoardId(1, 100))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.EDITOR).build()));
        when(boardMemberRepository.findById(2))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.VIEWER).build()));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 10))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build()));

        assertThat(boardAuth.canModerate(100, 2)).isFalse();
    }

    @Test
    void canModerate_shouldReturnFalse_WhenAnyRoleIsNull() {
        mockSecurityContextWithUserId(1);

        Workspace workspace = Workspace.builder().id(10).build();
        Board board = Board.builder().id(100).workspace(workspace).build();

        when(boardRepository.findById(100)).thenReturn(Optional.of(board));
        when(boardMemberRepository.findByUserIdAndBoardId(1, 100)).thenReturn(Optional.empty());
        when(boardMemberRepository.findById(2)).thenReturn(Optional.empty());
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 10)).thenReturn(Optional.empty());

        assertThat(boardAuth.canModerate(100, 2)).isFalse();
    }

    @Test
    void canDelete_shouldReturnTrue_WhenUserIsBoardAdmin() {
        mockSecurityContextWithUserId(1);

        Workspace workspace = Workspace.builder().id(10).build();
        Board board = Board.builder().id(100).workspace(workspace).build();

        when(boardRepository.findById(100)).thenReturn(Optional.of(board));
        when(boardMemberRepository.findByUserIdAndBoardId(1, 100))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.ADMIN).build()));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 10))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build()));

        assertThat(boardAuth.canDelete(100)).isTrue();
    }

    @Test
    void canDelete_shouldReturnTrue_WhenUserIsWorkspaceOwnerOrAdmin() {
        mockSecurityContextWithUserId(1);

        Workspace workspace = Workspace.builder().id(10).build();
        Board board = Board.builder().id(100).workspace(workspace).build();

        when(boardRepository.findById(100)).thenReturn(Optional.of(board));
        when(boardMemberRepository.findByUserIdAndBoardId(1, 100)).thenReturn(Optional.empty());
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 10))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.OWNER).build()));

        assertThat(boardAuth.canDelete(100)).isTrue();
    }

    @Test
    void canDelete_shouldReturnFalse_WhenUserIsNotAdminOrOwner() {
        mockSecurityContextWithUserId(1);

        Workspace workspace = Workspace.builder().id(10).build();
        Board board = Board.builder().id(100).workspace(workspace).build();

        when(boardRepository.findById(100)).thenReturn(Optional.of(board));
        when(boardMemberRepository.findByUserIdAndBoardId(1, 100))
                .thenReturn(Optional.of(BoardMember.builder().role(BoardRole.VIEWER).build()));
        when(workspaceMemberRepository.findByUserIdAndWorkspaceId(1, 10))
                .thenReturn(Optional.of(WorkspaceMember.builder().role(WorkspaceRole.MEMBER).build()));

        assertThat(boardAuth.canDelete(100)).isFalse();
    }
}