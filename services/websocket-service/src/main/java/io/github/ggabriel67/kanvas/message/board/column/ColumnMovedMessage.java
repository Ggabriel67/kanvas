package io.github.ggabriel67.kanvas.message.board.column;

public record ColumnMovedMessage(
        Integer columnId,
        double newOrderIndex
) {
}
