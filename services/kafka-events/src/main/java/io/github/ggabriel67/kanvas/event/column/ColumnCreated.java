package io.github.ggabriel67.kanvas.event.column;

public record ColumnCreated(
        Integer columnId,
        double orderIndex,
        String name,
        Integer boardId
) {
}
