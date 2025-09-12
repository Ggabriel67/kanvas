package io.github.ggabriel67.kanvas.board.member;

public record BoardMemberRemoveRequest(
        Integer targetMemberId,
        Integer boardId
) {
}
