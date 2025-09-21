package io.github.ggabriel67.kanvas.message.board.task;

import java.time.Instant;

public record TaskUpdatedMessage(
        Integer taskId,
        String title,
        Instant deadline,
        String priority,
        String taskStatus,
        boolean isExpired
) {
}
