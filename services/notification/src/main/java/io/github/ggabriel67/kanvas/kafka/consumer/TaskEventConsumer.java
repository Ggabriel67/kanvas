package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.task.TaskAssignment;
import io.github.ggabriel67.kanvas.event.task.TaskEventType;
import io.github.ggabriel67.kanvas.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskEventConsumer
{
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "task.events")
    public void consumeTaskEvent(Event<?> event) {
        log.info("Consuming message from 'task.events' topic");
        TaskEventType eventType = TaskEventType.valueOf(event.getEventType());
        switch (eventType) {
            case ASSIGNED, UNASSIGNED -> {
                TaskAssignment taskAssignment = objectMapper.convertValue(event.getPayload(), TaskAssignment.class);
                handleTaskAssignment(taskAssignment);
            }
            default -> {}
        }
    }

    private void handleTaskAssignment(TaskAssignment taskAssignment) {
        notificationService.createAssignmentNotification(taskAssignment);
    }
}
