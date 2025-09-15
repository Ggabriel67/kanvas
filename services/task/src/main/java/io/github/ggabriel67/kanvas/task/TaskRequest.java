package io.github.ggabriel67.kanvas.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record TaskRequest(
        Integer columnId,
        @NotNull(message = "Task name is required")
        @NotBlank(message = "Task name is required")
        @Size(max = 255, message = "Task name is too long")
        String title,
        @Size(max = 4096, message = "Description is too long")
        String description,
        Instant deadline,
        TaskPriority priority,
        List<Integer> assigneeIds,
        List<Integer> userIds
) {
}
