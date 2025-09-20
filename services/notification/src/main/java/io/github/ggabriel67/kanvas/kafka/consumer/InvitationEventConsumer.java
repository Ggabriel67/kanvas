package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.event.invitation.InvitationEventType;
import io.github.ggabriel67.kanvas.event.invitation.InvitationUpdate;
import io.github.ggabriel67.kanvas.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationEventConsumer
{
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @KafkaListener(topics = "invitation.events")
    public void consumeInvitationEvent(Event<?> event) {
        log.info("Consuming message from 'invitation.events' topic");
        InvitationEventType eventType = InvitationEventType.valueOf(event.getEventType());
        switch (eventType) {
            case CREATED -> {
                InvitationCreated invitationCreated = objectMapper.convertValue(event.getPayload(), InvitationCreated.class);
                handleInvitationCreated(invitationCreated);
            }
            case UPDATED -> {
                InvitationUpdate invitationCreated = objectMapper.convertValue(event.getPayload(), InvitationUpdate.class);
                handleInvitationUpdated(invitationCreated);
            }
        }
    }

    private void handleInvitationCreated(InvitationCreated invitationCreated) {
        notificationService.createInvitationNotification(invitationCreated);
    }

    private void handleInvitationUpdated(InvitationUpdate invitationCreated) {
        notificationService.updateInvitationNotification(invitationCreated);
    }
}
