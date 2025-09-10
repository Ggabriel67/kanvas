package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;

public record WorkspaceMemberRemoveRequest(
        Integer memberId,
        Integer workspaceId,
        WorkspaceRole newRole
) {
}
