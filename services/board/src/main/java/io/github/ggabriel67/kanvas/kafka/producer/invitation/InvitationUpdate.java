package io.github.ggabriel67.kanvas.kafka.producer.invitation;

import io.github.ggabriel67.kanvas.invitation.InvitationStatus;

public record InvitationUpdate(
        Integer invitationId,
        Integer inviteeId,
        InvitationStatus status
) {
}
