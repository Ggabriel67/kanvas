package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.exception.NameAlreadyInUseException;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceService
{
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    public void createWorkspace(WorkspaceRequest request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (workspaceRepository.findByNameAndOwner(request.name(), owner).isPresent()) {
            throw new NameAlreadyInUseException("Workspace with name '" + request.name() + "' already exists");
        }

        workspaceRepository.save(
                Workspace.builder()
                        .owner(owner)
                        .name(request.name())
                        .description(request.description())
                        .build()
        );
    }

    public void updateWorkspace(WorkspaceRequest request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (workspaceRepository.findByNameAndOwner(request.name(), owner).isPresent()) {
            throw new NameAlreadyInUseException("Workspace with name " + request.name() + " already exists");
        }

        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                        .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        mergeWorkspace(workspace, request);
        workspaceRepository.save(workspace);
    }

    private void mergeWorkspace(Workspace workspace, WorkspaceRequest request) {
        workspace.setName(request.name());
        workspace.setDescription(request.description());
    }
}
