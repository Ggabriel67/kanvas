package io.github.ggabriel67.kanvas.column;

import io.github.ggabriel67.kanvas.task.TaskPriority;
import io.github.ggabriel67.kanvas.task.TaskStatus;

import java.time.Instant;

public record ColumnTaskFlatDto(
        Integer columnId,
        double columnOrderIndex,
        String columnName,
        Integer taskId,
        double taskOrderIndex,
        String taskTitle,
        Instant deadline,
        TaskStatus status,
        TaskPriority priority,
        Integer assigneeId
) {
}
