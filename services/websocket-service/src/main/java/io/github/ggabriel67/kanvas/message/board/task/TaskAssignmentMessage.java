package io.github.ggabriel67.kanvas.message.board.task;

public record TaskAssignmentMessage(
        Integer taskId,
        Integer boardMemberId
) {
}
