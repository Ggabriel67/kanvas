package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;

public record BoardRoleChangeRequest(
        Integer targetMemberId,
        Integer boardId,
        BoardRole newRole
) {
}
