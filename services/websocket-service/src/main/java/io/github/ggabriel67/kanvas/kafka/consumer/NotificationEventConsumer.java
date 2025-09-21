package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.notification.NotificationCreated;
import io.github.ggabriel67.kanvas.event.notification.NotificationEventType;
import io.github.ggabriel67.kanvas.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventConsumer
{
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @KafkaListener(topics = "notification.events")
    public void consumeNotificationCreated(Event<?> event) {
        log.info("Consuming message from 'notification.events' topic");
        NotificationEventType eventType = NotificationEventType.valueOf(event.getEventType());
        if (eventType == NotificationEventType.CREATED) {
            NotificationCreated notificationCreated = objectMapper.convertValue(event.getPayload(), NotificationCreated.class);
            messageService.sendNotification(notificationCreated);
        }
    }
}
