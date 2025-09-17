package io.github.ggabriel67.kanvas.event.board;

public record BoardMemberRemoved(
        Integer memberId,
        Integer userId,
        Integer boardId,
        String boardName
) {
}
