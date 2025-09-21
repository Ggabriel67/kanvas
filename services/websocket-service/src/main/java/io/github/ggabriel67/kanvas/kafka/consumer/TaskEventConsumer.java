package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.task.*;
import io.github.ggabriel67.kanvas.message.MessageService;
import io.github.ggabriel67.kanvas.message.board.BoardMessage;
import io.github.ggabriel67.kanvas.message.board.BoardMessageType;
import io.github.ggabriel67.kanvas.message.board.task.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventConsumer
{
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @KafkaListener(topics = "task.events")
    public void consumeTaskEvent(Event<?> event) {
        log.info("Consuming message from 'task.events' topic");
        TaskEventType eventType = TaskEventType.valueOf(event.getEventType());
        switch (eventType) {
            case CREATED -> {
                TaskCreated taskCreated = objectMapper.convertValue(event.getPayload(), TaskCreated.class);
                handleTaskCreated(taskCreated);
            }
            case UPDATED -> {
                TaskUpdated taskUpdated = objectMapper.convertValue(event.getPayload(), TaskUpdated.class);
                handleTaskUpdated(taskUpdated);
            }
            case MOVED -> {
                TaskMoved taskMoved = objectMapper.convertValue(event.getPayload(), TaskMoved.class);
                handleTaskMoved(taskMoved);
            }
            case DELETED -> {
                TaskDeleted taskDeleted = objectMapper.convertValue(event.getPayload(), TaskDeleted.class);
                handleTaskDeleted(taskDeleted);
            }
            case ASSIGNED, UNASSIGNED -> {
                TaskAssignment taskAssignment = objectMapper.convertValue(event.getPayload(), TaskAssignment.class);
                handleTaskAssignment(taskAssignment);
            }
            default -> {}
        }
    }

    private void handleTaskCreated(TaskCreated taskCreated) {
        BoardMessage<TaskCreatedMessage> message = new BoardMessage<>(
                BoardMessageType.TASK_CREATED,
                new TaskCreatedMessage(taskCreated.columnId(), taskCreated.taskId(), taskCreated.title())
        );
        messageService.sendBoardMessage(taskCreated.boardId(), message);
    }

    private void handleTaskUpdated(TaskUpdated taskUpdated) {
        BoardMessage<TaskUpdatedMessage> message = new BoardMessage<>(
                BoardMessageType.TASK_UPDATED,
                new TaskUpdatedMessage(taskUpdated.taskId(), taskUpdated.title(), taskUpdated.deadline(), taskUpdated.priority(),
                        taskUpdated.taskStatus(), taskUpdated.isExpired())
        );
        messageService.sendBoardMessage(taskUpdated.boardId(), message);
    }

    private void handleTaskMoved(TaskMoved taskMoved) {
        BoardMessage<TaskMovedMessage> message = new BoardMessage<>(
                BoardMessageType.TASK_MOVED,
                new TaskMovedMessage(taskMoved.beforeColumnId(), taskMoved.targetColumnId(), taskMoved.taskId(), taskMoved.newOrderIndex())
        );
        messageService.sendBoardMessage(taskMoved.boardId(), message);
    }

    private void handleTaskDeleted(TaskDeleted taskDeleted) {
        BoardMessage<TaskDeletedMessage> message = new BoardMessage<>(
                BoardMessageType.TASK_DELETED,
                new TaskDeletedMessage((taskDeleted.taskId()))
        );
        messageService.sendBoardMessage(taskDeleted.boardId(), message);
    }

    private void handleTaskAssignment(TaskAssignment taskAssignment) {
        BoardMessageType messageType = taskAssignment.assigned() ? BoardMessageType.TASK_ASSIGNED : BoardMessageType.TASK_UNASSIGNED;
        BoardMessage<TaskAssignmentMessage> message = new BoardMessage<>(
                messageType,
                new TaskAssignmentMessage(taskAssignment.taskId(), taskAssignment.boardMemberId())
        );
    }
}
