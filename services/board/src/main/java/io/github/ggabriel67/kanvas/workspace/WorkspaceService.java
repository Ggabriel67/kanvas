package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;
import io.github.ggabriel67.kanvas.exception.NameAlreadyInUseException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberService;
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
        User owner = userService.getUserById(request.ownerId());

        if (workspaceRepository.findByNameAndCreatedBy(request.name(), owner).isPresent()) {
            throw new NameAlreadyInUseException("Workspace with name '" + request.name() + "' already exists");
        }

        Workspace workspace = workspaceRepository.save(
                Workspace.builder()
                        .createdBy(owner)
                        .name(request.name())
                        .description(request.description())
                        .build()
        );

        memberRepository.save(
                WorkspaceMember.builder()
                        .user(owner)
                        .workspace(workspace)
                        .role(WorkspaceRole.OWNER)
                        .build()
        );
    }

    public void updateWorkspace(WorkspaceRequest request, Integer workspaceId) {
        User owner = userService.getUserById(request.ownerId());

        if (workspaceRepository.findByNameAndCreatedBy(request.name(), owner).isPresent()) {
            throw new NameAlreadyInUseException("Workspace with name " + request.name() + " already exists");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                        .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

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
