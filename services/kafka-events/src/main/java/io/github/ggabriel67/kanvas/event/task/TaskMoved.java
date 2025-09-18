package io.github.ggabriel67.kanvas.event.task;

public record TaskMoved(
        Integer boardId,
        Integer targetColumnId,
        Integer taskId,
        double newOrderIndex
) {
}
