package io.github.ggabriel67.kanvas.kafka.producer;

import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.event.invitation.InvitationEventType;
import io.github.ggabriel67.kanvas.event.workspace.WorkspaceDeleted;
import io.github.ggabriel67.kanvas.event.workspace.WorkspaceEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceEventProducer
{
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendWorkspaceDeleted(WorkspaceDeleted workspaceDeleted) {
        Message<Event<WorkspaceDeleted>> message = MessageBuilder
                .withPayload(new Event<>(WorkspaceEventType.WORKSPACE_DELETED.name(), workspaceDeleted))
                .setHeader(KafkaHeaders.TOPIC, "workspace.events")
                .build();
        kafkaTemplate.send(message);
    }
}
