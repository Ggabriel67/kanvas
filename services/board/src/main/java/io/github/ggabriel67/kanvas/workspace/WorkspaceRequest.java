package io.github.ggabriel67.kanvas.workspace;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(
        Integer creatorId,
        @NotEmpty(message = "Name cannot be empty")
        @NotBlank(message = "Name cannot be empty")
        @Size(max = 64, message = "Name is too long")
        String name,
        @Size(max = 4096, message = "Description is too long")
        String description
)
{

}
