package io.github.ggabriel67.kanvas.task;

import jakarta.validation.constraints.Size;

import java.time.Instant;

public record TaskUpdateRequest(
        Integer taskId,
        String title,
        @Size(max = 4096, message = "Description is too long")
        String description,
        Instant deadline,
        TaskPriority priority,
        TaskStatus status
) {
}
