package io.github.ggabriel67.kanvas.kafka.producer.board;

import io.github.ggabriel67.kanvas.kafka.event.Event;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendBoardDeleted(Integer boardId) {
        log.info("Sending board deleted");
        Message<Event<Object>> message = MessageBuilder
                .withPayload(Event.builder()
                        .eventType(String.valueOf(BoardEventType.DELETED))
                        .payload(boardId)
                        .build())
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendRoleChanged(RoleChanged roleChanged) {
        log.info("Sending role changed");
        Message<Event<Object>> message = MessageBuilder
                .withPayload(Event.builder()
                        .eventType(String.valueOf(BoardEventType.DELETED))
                        .payload(roleChanged)
                        .build())
                .setHeader(KafkaHeaders.TOPIC, "board.events")
                .build();
        kafkaTemplate.send(message);
    }
}
