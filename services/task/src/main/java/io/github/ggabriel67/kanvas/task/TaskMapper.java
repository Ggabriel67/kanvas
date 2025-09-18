package io.github.ggabriel67.kanvas.task;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskMapper
{
    public TaskDto toTaskDto(Task task, List<Integer> assigneeIds) {
        return new TaskDto(
                task.getId(),
                task.getOrderIndex(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getDeadline(),
                task.getStatus(),
                task.getPriority(),
                assigneeIds,
                task.isExpired()
        );
    }
}
