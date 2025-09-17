package io.github.ggabriel67.kanvas.event.board;

public record MemberRemoved(
        Integer memberId,
        Integer userId,
        Integer boardId
) {
}
