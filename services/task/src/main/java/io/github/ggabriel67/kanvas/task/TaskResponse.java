package io.github.ggabriel67.kanvas.task;

public record TaskResponse(
        Integer taskId,
        Integer columnId,
        double orderIndex,
        boolean isExpired
) {
}
