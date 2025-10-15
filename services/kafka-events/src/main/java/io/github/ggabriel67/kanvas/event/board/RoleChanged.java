package io.github.ggabriel67.kanvas.event.board;

public record  RoleChanged(
        Integer boardId,
        Integer memberId,
        String role
) {
}
