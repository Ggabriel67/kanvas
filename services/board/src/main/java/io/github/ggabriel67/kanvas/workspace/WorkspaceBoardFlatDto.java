package io.github.ggabriel67.kanvas.workspace;

public record WorkspaceBoardFlatDto(
        Integer workspaceId,
        String workspaceName,
        Integer boardId,
        String boardName
) {
}
