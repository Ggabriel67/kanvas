package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.board.BoardDtoProjection;

import java.time.LocalDateTime;
import java.util.List;

public record WorkspaceDto(
    Integer id,
    String name,
    String description,
    LocalDateTime createdAt,
    List<BoardDtoProjection> boardProjections
)
{

}
