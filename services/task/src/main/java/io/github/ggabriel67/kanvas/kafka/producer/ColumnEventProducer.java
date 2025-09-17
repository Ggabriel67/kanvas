package io.github.ggabriel67.kanvas.kafka.producer;

import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.column.*;
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
public class ColumnEventProducer
{
    private final KafkaTemplate<String, Event<?>> kafkaTemplate;

    public void sendColumnCreated(ColumnCreated columnCreated) {
        log.info("Sending column created");
        Message<Event<ColumnCreated>> message = MessageBuilder
                .withPayload(new Event<>(ColumnEventType.CREATED.name(), columnCreated))
                .setHeader(KafkaHeaders.TOPIC, "column.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendColumnUpdated(ColumnUpdated columnUpdated) {
        log.info("Sending column updated");
        Message<Event<ColumnUpdated>> message = MessageBuilder
                .withPayload(new Event<>(ColumnEventType.UPDATED.name(), columnUpdated))
                .setHeader(KafkaHeaders.TOPIC, "column.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendColumnMoved(ColumnMoved columnMoved) {
        log.info("Sending column moved");
        Message<Event<ColumnMoved>> message = MessageBuilder
                .withPayload(new Event<>(ColumnEventType.MOVED.name(), columnMoved))
                .setHeader(KafkaHeaders.TOPIC, "column.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendColumnDeleted(ColumnDeleted columnDeleted) {
        log.info("Sending column deleted");
        Message<Event<ColumnDeleted>> message = MessageBuilder
                .withPayload(new Event<>(ColumnEventType.DELETED.name(), columnDeleted))
                .setHeader(KafkaHeaders.TOPIC, "column.events")
                .build();
        kafkaTemplate.send(message);
    }
}
