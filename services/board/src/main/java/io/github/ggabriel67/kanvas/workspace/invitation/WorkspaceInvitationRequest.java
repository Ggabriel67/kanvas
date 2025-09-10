package io.github.ggabriel67.kanvas.workspace.invitation;

public record WorkspaceInvitationRequest(
        Integer inviterId,
        Integer inviteeId,
        Integer workspaceId
) {

}
