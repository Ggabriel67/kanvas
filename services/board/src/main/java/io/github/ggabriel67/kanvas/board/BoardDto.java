package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.board.member.BoardMemberDto;
import io.github.ggabriel67.kanvas.feign.ColumnDto;

import java.time.LocalDateTime;
import java.util.List;

public record BoardDto(
        Integer boardId,
        String name,
        String description,
        LocalDateTime createdAt,
        BoardVisibility visibility,
        boolean readonly,
        List<BoardMemberDto> boardMembers,
        List<ColumnDto> columns
) {
}
