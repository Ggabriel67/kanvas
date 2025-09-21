package io.github.ggabriel67.kanvas.message.board.column;

public record ColumnCreatedMessage(
        Integer columnId,
        double orderIndex,
        String name
) {
}
