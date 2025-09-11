package io.github.ggabriel67.kanvas.board.invitation;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;

public record BoardInvitationRequest(
        Integer inviterId,
        Integer inviteeId,
        Integer boardId,
        BoardRole role
) {
}
