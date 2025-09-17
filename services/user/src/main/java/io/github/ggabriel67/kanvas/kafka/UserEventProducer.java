package io.github.ggabriel67.kanvas.kafka;

import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.user.UserCreated;
import io.github.ggabriel67.kanvas.event.user.UserEventType;
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
public class UserEventProducer
{
    private final KafkaTemplate<String, Event<?>> kafkaTemplate;

    public void sendUserCreated(UserCreated userCreated) {
        log.info("Sending user created");
        Message<Event<UserCreated>> message = MessageBuilder
                .withPayload(new Event<>(UserEventType.CREATED.name(), userCreated))
                .setHeader(KafkaHeaders.TOPIC, "user.events")
                .build();
        kafkaTemplate.send(message);
    }
}
