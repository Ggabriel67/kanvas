package io.github.ggabriel67.kanvas.workspace.member;

import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;

import java.time.LocalDateTime;

public record WorkspaceMemberDto(
        Integer memberId,
        Integer userId,
        String firstname,
        String lastname,
        String username,
        String avatarColor,
        WorkspaceRole role,
        LocalDateTime joinedAt
) {

}
