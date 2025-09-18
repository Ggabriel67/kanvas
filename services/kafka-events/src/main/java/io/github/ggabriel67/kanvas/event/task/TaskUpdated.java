package io.github.ggabriel67.kanvas.event.task;

import java.time.Instant;

public record TaskUpdated(
        Integer boardId,
        Integer taskId,
        String title,
        Instant deadline,
        String priority,
        String taskStatus,
        boolean isExpired
) {
}
