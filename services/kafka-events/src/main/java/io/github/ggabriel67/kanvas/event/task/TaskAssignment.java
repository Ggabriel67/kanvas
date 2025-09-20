package io.github.ggabriel67.kanvas.event.task;

public record TaskAssignment(
        Integer boardId,
        Integer taskId,
        Integer boardMemberId,
        Integer userId,
        String taskTitle,
        String boardName,
        boolean assigned
) {
}
