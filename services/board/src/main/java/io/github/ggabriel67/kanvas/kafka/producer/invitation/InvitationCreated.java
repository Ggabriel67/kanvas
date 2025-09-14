package io.github.ggabriel67.kanvas.kafka.producer.invitation;

import io.github.ggabriel67.kanvas.invitation.InvitationScope;

public record InvitationCreated(
    Integer invitationId,
    Integer inviteeId,
    String inviterUsername,
    String targetName,
    InvitationScope scope
) {
}
