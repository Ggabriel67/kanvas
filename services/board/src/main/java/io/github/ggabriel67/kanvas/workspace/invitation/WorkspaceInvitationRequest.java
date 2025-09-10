package io.github.ggabriel67.kanvas.workspace.invitation;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;

public record WorkspaceInvitationRequest(
        Integer inviterId,
        Integer inviteeId,
        Integer workspaceId,
        WorkspaceRole role
) {

}
