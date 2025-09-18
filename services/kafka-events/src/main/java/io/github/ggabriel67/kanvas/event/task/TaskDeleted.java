package io.github.ggabriel67.kanvas.event.task;

public record TaskDeleted(
        Integer boardId,
        Integer taskId
) {
}
