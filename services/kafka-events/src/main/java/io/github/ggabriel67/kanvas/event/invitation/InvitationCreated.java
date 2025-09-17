package io.github.ggabriel67.kanvas.event.invitation;

public record InvitationCreated(
    Integer invitationId,
    Integer inviteeId,
    String inviterUsername,
    String targetName,
    String scope
) {
}
