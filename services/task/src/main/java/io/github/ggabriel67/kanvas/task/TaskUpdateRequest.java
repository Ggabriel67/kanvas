package io.github.ggabriel67.kanvas.task;

import java.time.Instant;

public record TaskUpdateRequest(
        Integer taskId,
        String title,
        String description,
        Instant deadline,
        TaskPriority priority,
        TaskStatus status
) {
}
