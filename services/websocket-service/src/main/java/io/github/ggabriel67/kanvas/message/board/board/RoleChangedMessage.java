package io.github.ggabriel67.kanvas.message.board.board;

public record RoleChangedMessage(
        Integer memberId,
        String role
) {
}
