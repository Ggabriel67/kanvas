package io.github.ggabriel67.kanvas.kafka.producer;

import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.task.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskEventProducer
{
    private final KafkaTemplate<String, Event<?>> kafkaTemplate;

    public void sendTaskCreated(TaskCreated taskCreated) {
        log.info("Sending task created");
        Message<Event<TaskCreated>> message = MessageBuilder
                .withPayload(new Event<>(TaskEventType.CREATED.name(), taskCreated))
                .setHeader(KafkaHeaders.TOPIC, "task.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendTaskUpdated(TaskUpdated taskUpdated) {
        log.info("Sending task updated");
        Message<Event<TaskUpdated>> message = MessageBuilder
                .withPayload(new Event<>(TaskEventType.UPDATED.name(), taskUpdated))
                .setHeader(KafkaHeaders.TOPIC, "task.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendTaskMoved(TaskMoved taskMoved) {
        log.info("Sending task moved");
        Message<Event<TaskMoved>> message = MessageBuilder
                .withPayload(new Event<>(TaskEventType.MOVED.name(), taskMoved))
                .setHeader(KafkaHeaders.TOPIC, "task.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendTaskDeleted(TaskDeleted taskDeleted) {
        log.info("Sending task deleted");
        Message<Event<TaskDeleted>> message = MessageBuilder
                .withPayload(new Event<>(TaskEventType.DELETED.name(), taskDeleted))
                .setHeader(KafkaHeaders.TOPIC, "task.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendTaskAssigned(TaskAssignment taskAssignment) {
        log.info("Sending task assigned");
        Message<Event<TaskAssignment>> message = MessageBuilder
                .withPayload(new Event<>(TaskEventType.ASSIGNED.name(), taskAssignment))
                .setHeader(KafkaHeaders.TOPIC, "task.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendTaskUnassigned(TaskAssignment taskAssignment) {
        log.info("Sending task unassigned");
        Message<Event<TaskAssignment>> message = MessageBuilder
                .withPayload(new Event<>(TaskEventType.UNASSIGNED.name(), taskAssignment))
                .setHeader(KafkaHeaders.TOPIC, "task.events")
                .build();
        kafkaTemplate.send(message);
    }
}
