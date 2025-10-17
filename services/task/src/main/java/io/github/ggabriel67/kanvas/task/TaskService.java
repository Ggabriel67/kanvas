package io.github.ggabriel67.kanvas.task;

import io.github.ggabriel67.kanvas.column.Column;
import io.github.ggabriel67.kanvas.column.ColumnRepository;
import io.github.ggabriel67.kanvas.event.task.TaskCreated;
import io.github.ggabriel67.kanvas.event.task.TaskDeleted;
import io.github.ggabriel67.kanvas.event.task.TaskMoved;
import io.github.ggabriel67.kanvas.event.task.TaskUpdated;
import io.github.ggabriel67.kanvas.exception.ColumnNotFoundException;
import io.github.ggabriel67.kanvas.exception.TaskNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.TaskEventProducer;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssignee;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeRepository;
import lombok.Getter;
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
    private final TaskEventProducer taskEventProducer;

    @Getter
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
                        .description(null)
                        .deadline(null)
                        .priority(null)
                        .build()
        );

        taskEventProducer.sendTaskCreated(new TaskCreated(
                column.getBoardId(), column.getId(), task.getId(), orderIndex, task.getTitle())
        );

        return new TaskResponse(task.getId(), column.getId(), task.getOrderIndex(), false);
    }

    public TaskResponse updateTask(TaskUpdateRequest request) {
        Task task = getTaskById(request.taskId());

        mergeTask(task, request);
        taskRepository.save(task);

        String priority = request.priority() != null ? request.priority().name() : null;
        String status = request.status() != null ? request.status().name() : null;
        taskEventProducer.sendTaskUpdated(new TaskUpdated(task.getColumn().getBoardId(), task.getId(),
                request.title(), request.deadline(), priority, status, task.isExpired()
        ));

        return new TaskResponse(task.getId(), task.getColumn().getId(), task.getOrderIndex(), task.isExpired());
    }

    private void mergeTask(Task task, TaskUpdateRequest request) {
        if (request.title() != null && StringUtils.isNotBlank(request.title())) task.setTitle(request.title());
        if (request.description() != null && StringUtils.isNotBlank(request.description())) task.setDescription(request.description());
        if (request.deadline() != null) task.setDeadline(request.deadline());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.status() != null) task.setStatus(request.status());
    }

    @Transactional
    public TaskResponse moveTask(MoveTaskRequest request) {
        double newOrderIndex;
        Column targetColumn = columnRepository.findById(request.targetColumnId())
                .orElseThrow(() -> new ColumnNotFoundException("Column not found"));

        Task preceding, following;
        if (request.precedingTaskId() == null && request.followingTaskId() == null) {
            newOrderIndex = step;
        }
        else if (request.precedingTaskId() == null) {
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
        Integer beforeColumnId = task.getColumn().getId();
        if (!task.getColumn().equals(targetColumn)) task.setColumn(targetColumn);
        taskRepository.save(task);

        taskEventProducer.sendTaskMoved(new TaskMoved(
                targetColumn.getBoardId(), beforeColumnId, targetColumn.getId(), task.getId(), newOrderIndex)
        );

        return new TaskResponse(task.getId(), targetColumn.getId(), newOrderIndex, task.isExpired());
    }

    public boolean isTaskExpired(TaskStatus status, Instant deadline) {
        return status != TaskStatus.DONE && deadline != null &&
                deadline.isBefore(Instant.now());
    }

    @Transactional
    public void deleteTask(Integer taskId) {
        Task task = getTaskById(taskId);

        TaskDeleted taskDeleted = new TaskDeleted(task.getColumn().getBoardId(), taskId);

        taskAssigneeRepository.deleteAllByTaskId(taskId);
        taskRepository.deleteById(taskId);

        taskEventProducer.sendTaskDeleted(taskDeleted);
    }

    public TaskDto getTask(Integer taskId) {
        Task task = getTaskById(taskId);
        var assigneeIds = taskAssigneeRepository.findTaskAssigneeIdsByTaskId(taskId);
        return taskMapper.toTaskDto(task, assigneeIds);
    }
}
