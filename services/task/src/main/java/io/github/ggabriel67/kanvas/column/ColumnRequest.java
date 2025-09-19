package io.github.ggabriel67.kanvas.column;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ColumnRequest(
        Integer boardId,
        @NotNull(message = "Column name is required")
        @NotBlank(message = "Column name is required")
        @Size(max = 255, message = "Column name is too long")
        String name
) {

}
