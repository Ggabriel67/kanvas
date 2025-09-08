package io.github.ggabriel67.kanvas.workspace;

import java.time.LocalDateTime;

public record WorkspaceDto(
    Integer id,
    Integer ownerId,
    String name,
    String description,
    LocalDateTime createdAt
)
{

}
