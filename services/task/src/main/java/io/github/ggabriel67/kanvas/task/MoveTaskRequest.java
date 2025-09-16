package io.github.ggabriel67.kanvas.task;

public record MoveTaskRequest(
        Integer targetColumnId,
        Integer taskId,
        Integer precedingTaskId,
        Integer followingTaskId
) {
}
