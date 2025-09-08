package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;

public record WorkspaceMemberRequest(
        Integer userId,
        Integer workspaceId,
        WorkspaceRole role
) {

}
