package io.github.ggabriel67.kanvas.message.board.column;

public record ColumnUpdatedMessage(
        Integer columnId,
        String columnName
) {
}
