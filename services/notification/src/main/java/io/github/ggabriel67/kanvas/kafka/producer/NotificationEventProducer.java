package io.github.ggabriel67.kanvas.kafka.producer;

import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.notification.NotificationCreated;
import io.github.ggabriel67.kanvas.event.notification.NotificationEventType;
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
public class NotificationEventProducer
{
    private final KafkaTemplate<String, Event<?>> kafkaTemplate;

    public void sendNotificationCreated(NotificationCreated notificationCreated) {
        log.info("Sending notification created");
        Message<Event<NotificationCreated>> message = MessageBuilder
                .withPayload(new Event<>(NotificationEventType.CREATED.name(), notificationCreated))
                .setHeader(KafkaHeaders.TOPIC, "notification.events")
                .build();
        kafkaTemplate.send(message);
    }
}
