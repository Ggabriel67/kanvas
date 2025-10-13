package io.github.ggabriel67.kanvas.kafka.producer;

import io.github.ggabriel67.kanvas.event.board.*;
import io.github.ggabriel67.kanvas.event.Event;
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
public class BoardEventProducer
{
    private final KafkaTemplate<String, Event<?>> kafkaTemplate;

    public void sendBoardDeleted(BoardDeleted boardDeleted) {
        log.info("Sending board deleted");
        Message<Event<BoardDeleted>> message = MessageBuilder
                .withPayload(new Event<>(BoardEventType.DELETED.name(), boardDeleted))
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendRoleChanged(RoleChanged roleChanged) {
        log.info("Sending role changed");
        Message<Event<RoleChanged>> message = MessageBuilder
                .withPayload(new Event<>(BoardEventType.ROLE_CHANGED.name(), roleChanged))
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendMemberRemoved(BoardMemberRemoved memberRemoved) {
        log.info("Sending member removed");
        Message<Event<BoardMemberRemoved>> message = MessageBuilder
                .withPayload(new Event<>(BoardEventType.MEMBER_REMOVED.name(), memberRemoved))
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendBoardUpdated(BoardUpdated boardUpdated) {
        log.info("Sending board updated");
        Message<Event<BoardUpdated>> message = MessageBuilder
                .withPayload(new Event<>(BoardEventType.BOARD_UPDATED.name(), boardUpdated))
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendMemberJoined(BoardMemberJoined memberJoined) {
        log.info("Sending member joined");
        Message<Event<BoardMemberJoined>> message = MessageBuilder
                .withPayload(new Event<>(BoardEventType.MEMBER_JOINED.name(), memberJoined))
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendMemberLeft(BoardMemberRemoved memberRemoved) {
        log.info("Sending member left");
        Message<Event<BoardMemberRemoved>> message = MessageBuilder
                .withPayload(new Event<>(BoardEventType.MEMBER_LEFT.name(), memberRemoved))
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }
}
