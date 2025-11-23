package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.authorization.workspace.WorkspaceRole;

import java.time.LocalDateTime;

public record MemberDto(
        Integer memberId,
        Integer userId,
        String firstname,
        String lastname,
        String username,
        String avatarColor,
        BoardRole boardRole,
        WorkspaceRole workspaceRole,
        LocalDateTime joinedAt
) {
}
