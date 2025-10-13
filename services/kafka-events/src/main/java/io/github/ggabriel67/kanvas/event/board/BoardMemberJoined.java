package io.github.ggabriel67.kanvas.event.board;

public record BoardMemberJoined(
        Integer boardId,
        Integer memberId,
        Integer userId,
        String firstname,
        String lastname,
        String username,
        String avatarColor,
        String boardRole
) {
}
