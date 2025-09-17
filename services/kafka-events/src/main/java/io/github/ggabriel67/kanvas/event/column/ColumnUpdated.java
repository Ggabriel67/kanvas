package io.github.ggabriel67.kanvas.event.column;

public record ColumnUpdated(
        Integer boardId,
        Integer columnId,
        String columnName
) {
}
