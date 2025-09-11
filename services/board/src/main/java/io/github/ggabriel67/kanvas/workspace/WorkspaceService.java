package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceAuthorization;
import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.board.BoardDtoProjection;
import io.github.ggabriel67.kanvas.board.BoardService;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService
{
    private final WorkspaceAuthorization workspaceAuth;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserService userService;
    private final BoardService boardService;

    public void createWorkspace(WorkspaceRequest request) {
        User user = userService.getUserById(request.ownerId());

        Workspace workspace = workspaceRepository.save(
                Workspace.builder()
                        .createdBy(user)
                        .name(request.name())
                        .description(request.description())
                        .build()
        );

        workspaceMemberRepository.save(
                WorkspaceMember.builder()
                        .user(user)
                        .workspace(workspace)
                        .role(WorkspaceRole.OWNER)
                        .build()
        );
    }

    public void updateWorkspace(WorkspaceRequest request, Integer workspaceId) {
        Workspace workspace = this.getWorkspaceById(workspaceId);

        mergeWorkspace(workspace, request);
        workspaceRepository.save(workspace);
    }

    private void mergeWorkspace(Workspace workspace, WorkspaceRequest request) {
        workspace.setName(request.name());
        workspace.setDescription(request.description());
    }

    public Workspace getWorkspaceById(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
    }

    public WorkspaceDto getWorkspace(Integer workspaceId) {
        Integer userId = workspaceAuth.getCurrentUserId();

        Workspace workspace = getWorkspaceById(workspaceId);
        WorkspaceRole workspaceRole = workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, workspaceId)
                .map(WorkspaceMember::getRole)
                .orElseThrow(() -> new ForbiddenException("Not a member"));

        List<BoardDtoProjection> boardProjections;
        if (workspaceRole == WorkspaceRole.OWNER || workspaceRole == WorkspaceRole.ADMIN) {
            boardProjections = boardService.getBoardsByWorkspace(workspace);
        }
        else {
            boardProjections = boardService.getVisibleBoardsForMember(userId, workspace);
        }

        return new WorkspaceDto(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getCreatedAt(),
                boardProjections
        );
    }
}
