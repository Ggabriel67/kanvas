package io.github.ggabriel67.kanvas.message.board.task;

public record TaskMovedMessage(
        Integer beforeColumnId,
        Integer targetColumnId,
        Integer taskId,
        double newOrderIndex
) {
}
