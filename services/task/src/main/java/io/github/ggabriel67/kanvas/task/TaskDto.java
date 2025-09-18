package io.github.ggabriel67.kanvas.task;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record TaskDto(
        Integer taskId,
        double orderIndex,
        Integer columnId,
        String title,
        String description,
        LocalDateTime createdAt,
        Instant deadline,
        TaskStatus status,
        TaskPriority priority,
        List<Integer> assigneeIds,
        boolean isExpired
) {
}
