package io.github.ggabriel67.kanvas.event.task;

public record TaskCreated(
    Integer boardId,
    Integer columnId,
    Integer taskId,
    String title
) {
}
