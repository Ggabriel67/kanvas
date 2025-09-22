package io.github.ggabriel67.kanvas.message.board.task;

public record TaskCreatedMessage(
        Integer columnId,
        Integer taskId,
        double orderIndex,
        String title
) {
}
