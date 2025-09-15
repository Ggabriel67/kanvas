package io.github.ggabriel67.kanvas.task;

import java.time.Instant;
import java.util.List;

public record TaskDtoProjection(
    Integer taskId,
    Integer columnId,
    String name,
    Instant deadline,
    TaskStatus status,
    List<Integer> assigneeIds,
    boolean isExpired
) {
}
