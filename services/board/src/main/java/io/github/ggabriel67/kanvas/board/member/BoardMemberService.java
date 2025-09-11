package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardMemberService
{
    private final BoardMemberRepository memberRepository;

    public void addBoardMember(Board board, User invitee, BoardRole role) {
        memberRepository.save(
                BoardMember.builder()
                        .user(invitee)
                        .board(board)
                        .role(role)
                        .build()
        );
    }
}
