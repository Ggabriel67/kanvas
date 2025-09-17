package io.github.ggabriel67.kanvas.event.column;

public record ColumnMoved(
        Integer boardId,
        Integer columnId,
        double newOrderIndex
) {
}
