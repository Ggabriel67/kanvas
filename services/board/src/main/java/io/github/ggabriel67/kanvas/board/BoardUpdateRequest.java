package io.github.ggabriel67.kanvas.board;

import jakarta.validation.constraints.Size;

public record BoardUpdateRequest(
        @Size(max = 255, message = "Name is too long")
        String name,
        @Size(max = 4096, message = "Description is too long")
        String description,
        BoardVisibility visibility
) {
}
