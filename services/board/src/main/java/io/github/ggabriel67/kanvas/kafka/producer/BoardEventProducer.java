package io.github.ggabriel67.kanvas.kafka.producer;

import io.github.ggabriel67.kanvas.event.board.BoardDeleted;
import io.github.ggabriel67.kanvas.event.board.BoardEventType;
import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.event.board.RoleChanged;
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
}
