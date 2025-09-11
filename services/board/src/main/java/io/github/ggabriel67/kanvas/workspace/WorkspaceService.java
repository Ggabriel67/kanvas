package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService
{
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserService userService;

    public void createWorkspace(WorkspaceRequest request) {
        User user = userService.getUserById(request.ownerId());

        Workspace workspace = workspaceRepository.save(
                Workspace.builder()
                        .createdBy(user)
                        .name(request.name())
                        .description(request.description())
                        .build()
        );

        memberRepository.save(
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
}
