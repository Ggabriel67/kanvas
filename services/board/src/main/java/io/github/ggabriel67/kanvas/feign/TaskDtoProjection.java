package io.github.ggabriel67.kanvas.feign;

import java.time.Instant;
import java.util.List;

public record TaskDtoProjection(
        Integer taskId,
        double orderIndex,
        Integer columnId,
        String title,
        Instant deadline,
        String status,
        String priority,
        List<Integer> assigneeIds,
        boolean isExpired
) {
}

