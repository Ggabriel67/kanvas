package io.github.ggabriel67.kanvas.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskUpdateRequest(
        @NotNull Integer taskId,
        String title,
        String description,
        Instant deadline,
        TaskPriority priority,
        TaskStatus status
) {}