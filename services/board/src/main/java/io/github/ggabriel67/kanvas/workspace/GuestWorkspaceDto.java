package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.board.BoardDtoProjection;

import java.util.List;

public record GuestWorkspaceDto(
        Integer workspaceId,
        String workspaceName,
        List<BoardDtoProjection> boardProjections
) {

}
