package io.github.ggabriel67.kanvas.message.board.board;

import java.time.LocalDateTime;

public record MemberJoinedMessage(
        Integer memberId,
        Integer userId,
        String firstname,
        String lastname,
        String username,
        String avatarColor,
        String boardRole,
        LocalDateTime joinedAt
) {
}
