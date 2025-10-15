package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;

import java.time.LocalDateTime;

public record BoardMemberDto(
        Integer memberId,
        Integer userId,
        String firstname,
        String lastname,
        String username,
        String avatarColor,
        BoardRole boardRole,
        LocalDateTime joinedAt
) {
}
