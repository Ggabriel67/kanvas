package io.github.ggabriel67.kanvas.kafka.producer.invitation;

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
public class InvitationEventProducer
{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendInvitationCreated(InvitationCreated invitation) {
        log.info("Sending new invitation");
        Message<Event<Object>> message = MessageBuilder
                .withPayload(Event.builder()
                        .eventType(String.valueOf(InvitationEventType.SENT))
                        .payload(invitation)
                        .build())
                .setHeader(KafkaHeaders.TOPIC, "invitation.events")
                .build();
        kafkaTemplate.send(message);
    }

    public void sendInvitationUpdated(InvitationUpdate invitationUpdate) {
        log.info("Sending invitation updated");
        Message<Event<Object>> message = MessageBuilder
                .withPayload(Event.builder()
                        .eventType(String.valueOf(InvitationEventType.UPDATED))
                        .payload(invitationUpdate)
                        .build()).setHeader(KafkaHeaders.TOPIC, "invitation.events")
                .build();
        kafkaTemplate.send(message);
    }
}