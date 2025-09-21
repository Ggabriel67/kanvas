package io.github.ggabriel67.kanvas.event.board;

public record BoardUpdated(
        Integer boardId,
        String name,
        String description,
        String visibility
) {
}
