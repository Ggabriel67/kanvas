package io.github.ggabriel67.kanvas.task;

import io.github.ggabriel67.kanvas.column.Column;
import io.github.ggabriel67.kanvas.column.ColumnRepository;
import io.github.ggabriel67.kanvas.exception.ColumnNotFoundException;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssignee;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService
{
    private final TaskRepository taskRepository;
    private final ColumnRepository columnRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskMapper taskMapper;

    @Value("${application.ordering.step.task}")
    private Double step;

    public TaskDtoProjection createTask(TaskRequest request) {
        Column column = columnRepository.findById(request.columnId())
                .orElseThrow(() -> new ColumnNotFoundException("Column not found"));

        Double maxOrderIndex = taskRepository.findMaxOrderIndexByColumn(column);
        if (maxOrderIndex == null) {
            maxOrderIndex = 0d;
        }
        double orderIndex = maxOrderIndex + step;
        Task task = taskRepository.save(
                Task.builder()
                        .column(column)
                        .orderIndex(orderIndex)
                        .title(request.title())
                        .description(request.description())
                        .deadline(request.deadline())
                        .priority(request.priority())
                        .status(TaskStatus.ACTIVE)
                        .build()
        );
        List<TaskAssignee> taskAssignees = request.assigneeIds()
                .stream()
                .map(memberId -> TaskAssignee.builder()
                        .task(task)
                        .boardMemberId(memberId)
                        .build())
                .toList();

        if (!taskAssignees.isEmpty()) {
            taskAssigneeRepository.saveAll(taskAssignees);
        }

        return taskMapper.toTaskDtoProjection(task, request.assigneeIds());
    }
}
