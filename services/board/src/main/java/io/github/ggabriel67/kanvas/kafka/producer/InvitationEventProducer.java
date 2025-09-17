package io.github.ggabriel67.kanvas.kafka.producer;

import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.event.invitation.InvitationEventType;
import io.github.ggabriel67.kanvas.event.invitation.InvitationUpdate;
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
public class InvitationEventProducer
{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendInvitationCreated(InvitationCreated invitation) {
        log.info("Sending new invitation");
        Message<Event<InvitationCreated>> message = MessageBuilder
                .withPayload(new Event<>(InvitationEventType.SENT.name(), invitation))
                .setHeader(KafkaHeaders.TOPIC, "invitation.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendInvitationUpdated(InvitationUpdate invitationUpdate) {
        log.info("Sending invitation updated");
        Message<Event<InvitationUpdate>> message = MessageBuilder
                .withPayload(new Event<>(InvitationEventType.UPDATED.name(), invitationUpdate))
                .setHeader(KafkaHeaders.TOPIC, "invitation.events")
                .build();
        kafkaTemplate.send(message);
    }
}