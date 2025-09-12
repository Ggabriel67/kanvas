package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.board.member.BoardMemberDto;

import java.time.LocalDateTime;
import java.util.List;

public record BoardDto(
        Integer boardId,
        String name,
        String description,
        LocalDateTime createdAt,
        BoardVisibility visibility,
        List<BoardMemberDto> boardMembers
) {
}
