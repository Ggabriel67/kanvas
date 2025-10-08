package io.github.ggabriel67.kanvas.event.task;

public record TaskAssignment(
        Integer boardId,
        Integer taskId,
        Integer boardMemberId,
        Integer userId,
        Integer assignerId,
        String taskTitle,
        String boardName,
        boolean assigned
) {
}
