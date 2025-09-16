package io.github.ggabriel67.kanvas.task;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskMapper
{
    public TaskDtoProjection toTaskDtoProjection(Task task, List<Integer> assigneeIds) {
        return new TaskDtoProjection(
                task.getId(),
                task.getOrderIndex(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDeadline(),
                task.getStatus(),
                assigneeIds,
                task.isExpired()
        );
    }
}
