package io.github.ggabriel67.kanvas.message.board.board;

public record MemberJoinedMessage(
        Integer memberId,
        Integer userId,
        String firstname,
        String lastname,
        String username,
        String avatarColor,
        String boardRole
) {
}
