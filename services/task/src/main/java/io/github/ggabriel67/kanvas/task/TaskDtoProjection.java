package io.github.ggabriel67.kanvas.task;

import java.time.Instant;
import java.util.List;

public record TaskDtoProjection(
    Integer taskId,
    double orderIndex,
    Integer columnId,
    String title,
    Instant deadline,
    TaskStatus status,
    TaskPriority priority,
    List<Integer> assigneeIds,
    boolean isExpired
) {
}
