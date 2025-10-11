package io.github.ggabriel67.kanvas.event.invitation;

public record InvitationUpdate(
        Integer invitationId,
        Integer inviteeId,
        String status,
        String scope
) {
}
