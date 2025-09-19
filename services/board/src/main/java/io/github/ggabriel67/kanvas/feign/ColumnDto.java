package io.github.ggabriel67.kanvas.feign;

import java.util.List;

public record ColumnDto(
        Integer columnId,
        double orderIndex,
        String name,
        List<TaskDtoProjection> taskProjections
) {
}

