package io.github.ggabriel67.kanvas.kafka.producer.board;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;

public record RoleChanged(
        Integer memberId,
        BoardRole role
) {
}
