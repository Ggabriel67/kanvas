package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;

public record WorkspaceDtoProjection(
        Integer id,
        String name,
        WorkspaceRole role
) {

}
