package io.github.ggabriel67.kanvas.column;

import io.github.ggabriel67.kanvas.task.TaskDtoProjection;

import java.util.List;

public record ColumnDto(
        Integer columnId,
        double orderIndex,
        String name,
        List<TaskDtoProjection> taskProjections
) {
}
