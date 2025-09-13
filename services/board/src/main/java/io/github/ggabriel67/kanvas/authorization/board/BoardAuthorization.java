package io.github.ggabriel67.kanvas.authorization.board;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.board.BoardVisibility;
import io.github.ggabriel67.kanvas.board.member.BoardMember;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.exception.BoardNotFoundException;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("boardAuth")
@RequiredArgsConstructor
public class BoardAuthorization
{
    private final BoardMemberRepository boardMemberRepository;
    private final BoardRepository boardRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    public Integer getCurrentUserId() throws IllegalStateException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer userId) {
            return  userId;
        }
        throw new IllegalStateException("No user ID in SecurityContext");
    }

    public boolean hasRole(Integer userId, Integer boardId, BoardRole requiredRole) {
        return boardMemberRepository.findByUserIdAndBoardId(userId, boardId)
                .map(member -> switch (requiredRole) {
                    case ADMIN -> member.getRole() == BoardRole.ADMIN;
                    case EDITOR -> member.getRole() == BoardRole.ADMIN || member.getRole() == BoardRole.EDITOR;
                    case VIEWER -> true;
                })
                .orElse(false);
    }

    public boolean isAdmin(Integer boardId) {
        return hasRole(getCurrentUserId(), boardId, BoardRole.ADMIN);
    }

    public boolean canEdit(Integer boardId) {
        return hasRole(getCurrentUserId(), boardId, BoardRole.EDITOR);
    }

    public boolean canView(Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        Integer userId = getCurrentUserId();
        boolean isWorkspaceMember = workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, board.getWorkspace().getId())
                .isPresent();

        return hasRole(userId, boardId, BoardRole.VIEWER) ||
                (board.getVisibility() == BoardVisibility.WORKSPACE_PUBLIC && isWorkspaceMember);
    }

    public boolean canModerate(Integer boardId, Integer targetMemberId) {
        Integer callerId = getCurrentUserId();

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        BoardRole callerBoardRole = boardMemberRepository.findByUserIdAndBoardId(callerId, boardId)
                .map(BoardMember::getRole).orElse(null);

        BoardRole targetBoardRole = boardMemberRepository.findById(targetMemberId)
                .map(BoardMember::getRole).orElse(null);

        WorkspaceRole callerWorkspaceRole = workspaceMemberRepository.findByUserIdAndWorkspaceId(
                callerId, board.getWorkspace().getId())
                .map(WorkspaceMember::getRole).orElse(null);

        if (callerBoardRole == null || targetBoardRole == null || callerWorkspaceRole == null) {
            return false;
        }

        if (callerWorkspaceRole == WorkspaceRole.OWNER || callerWorkspaceRole == WorkspaceRole.ADMIN) {
            return true;
        }

        return callerBoardRole == BoardRole.ADMIN &&
                (targetBoardRole == BoardRole.EDITOR || targetBoardRole == BoardRole.VIEWER);
    }

    public boolean canUpdate(Integer boardId) {
        return hasRole(getCurrentUserId(), boardId, BoardRole.ADMIN);
    }

    public boolean canDelete(Integer boardId) {
        Integer callerId = getCurrentUserId();

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        WorkspaceRole workspaceRole = workspaceMemberRepository.findByUserIdAndWorkspaceId(
                        callerId, board.getWorkspace().getId())
                .map(WorkspaceMember::getRole).orElse(null);

        return hasRole(callerId, boardId, BoardRole.ADMIN) ||
                workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN;
    }
}
