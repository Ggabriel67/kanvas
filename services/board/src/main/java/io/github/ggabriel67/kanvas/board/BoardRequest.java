package io.github.ggabriel67.kanvas.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BoardRequest(
        Integer createdById,
        Integer workspaceId,
        @NotEmpty(message = "Name cannot be empty")
        @NotBlank(message = "Name cannot be empty")
        @Size(max = 255, message = "Name is too long")
        String name,
        String description,
        @Size(max = 4096, message = "Description is too long")
        BoardVisibility visibility
) {
}
