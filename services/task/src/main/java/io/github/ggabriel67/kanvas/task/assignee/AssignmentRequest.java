package io.github.ggabriel67.kanvas.task.assignee;

public record AssignmentRequest(
        Integer taskId,
        Integer boardId,
        Integer memberId
) {
}
