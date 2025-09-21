package io.github.ggabriel67.kanvas.message.board.board;

public record BoardUpdatedMessage(
        String name,
        String description,
        String visibility
) {
}
