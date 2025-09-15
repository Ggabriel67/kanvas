package io.github.ggabriel67.kanvas.board.member;

import org.springframework.stereotype.Service;

@Service
public class BoardMemberMapper {
    public BoardMemberDto toBoardMemberDto(BoardMember boardMember) {
        return new BoardMemberDto(
                boardMember.getId(),
                boardMember.getUser().getId(),
                boardMember.getUser().getFirstname(),
                boardMember.getUser().getLastname(),
                boardMember.getUser().getUsername(),
                boardMember.getUser().getAvatarColor(),
                boardMember.getRole()
        );
    }
}
