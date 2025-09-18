package io.github.ggabriel67.kanvas.task;

import io.github.ggabriel67.kanvas.column.Column;
import io.github.ggabriel67.kanvas.column.ColumnRepository;
import io.github.ggabriel67.kanvas.exception.ColumnNotFoundException;
import io.github.ggabriel67.kanvas.exception.TaskNotFoundException;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssignee;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
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

    private Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
    }

    public TaskResponse createTask(TaskRequest request) {
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
                        .status(TaskStatus.ACTIVE)
                        .build()
        );

        return new TaskResponse(task.getId(), column.getId(), task.getOrderIndex(), false);
    }

    @Transactional
    public TaskResponse moveTask(MoveTaskRequest request) {
        double newOrderIndex;
        Column targetColumn = columnRepository.findById(request.targetColumnId())
                .orElseThrow(() -> new ColumnNotFoundException("Column not found"));

        Task preceding, following;
        if (request.precedingTaskId() == null) {
            following = getTaskById(request.followingTaskId());
            newOrderIndex = following.getOrderIndex() - step;
        }
        else if (request.followingTaskId() == null) {
            preceding = getTaskById(request.precedingTaskId());
            newOrderIndex = preceding.getOrderIndex() + step;
        }
        else {
            preceding = getTaskById(request.precedingTaskId());
            following = getTaskById(request.followingTaskId());
            newOrderIndex = (preceding.getOrderIndex() + following.getOrderIndex()) / 2;
        }

        Task task = getTaskById(request.taskId());

        task.setOrderIndex(newOrderIndex);
        if (!task.getColumn().equals(targetColumn)) task.setColumn(targetColumn);
        taskRepository.save(task);
        return new TaskResponse(task.getId(), targetColumn.getId(), newOrderIndex, task.isExpired());
    }

    public boolean isTaskExpired(TaskStatus status, Instant deadline) {
        return status != TaskStatus.DONE && deadline != null &&
                deadline.isBefore(Instant.from(LocalDateTime.now()));
    }

    @Transactional
    public void deleteTask(Integer taskId) {
        taskAssigneeRepository.deleteAllByTaskId(taskId);
        taskRepository.deleteById(taskId);
    }

    public TaskResponse updateTask(TaskUpdateRequest request) {
        Task task = getTaskById(request.taskId());

        mergeTask(task, request);
        taskRepository.save(task);
        return new TaskResponse(task.getId(), task.getColumn().getId(), task.getOrderIndex(), task.isExpired());
    }

    private void mergeTask(Task task, TaskUpdateRequest request) {
        if (StringUtils.isNotBlank(request.title())) task.setTitle(request.title());
        if (StringUtils.isNotBlank(request.description())) task.setTitle(request.description());
        if (request.deadline() != null) task.setDeadline(request.deadline());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.status() != null) task.setStatus(request.status());
    }
}
