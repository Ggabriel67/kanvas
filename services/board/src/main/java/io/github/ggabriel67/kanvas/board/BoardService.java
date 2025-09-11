package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.exception.NameAlreadyInUseException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardService
{
    private final BoardRepository boardRepository;
    private final UserService userService;
    private final WorkspaceRepository workspaceRepository;

    public void createBoard(BoardRequest request) {
        User user = userService.getUserById(request.createdById());

        Workspace workspace = workspaceRepository.findById(request.workspaceId())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        if (boardRepository.findByNameAndWorkspace(request.name(), workspace).isPresent()) {
            throw new NameAlreadyInUseException("Board with name " +  request.name() + " already exists in this workspace");
        }

        boardRepository.save(
                Board.builder()
                        .createdBy(user)
                        .workspace(workspace)
                        .name(request.name())
                        .description(request.description())
                        .visibility(request.visibility())
                        .build()
        );
    }
}
