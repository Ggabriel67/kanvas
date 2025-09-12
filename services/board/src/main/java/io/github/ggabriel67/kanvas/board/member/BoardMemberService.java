package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.exception.BoardNotFoundException;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.exception.WorkspaceNotFoundException;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardMemberService
{
    private final BoardMemberRepository memberRepository;
    private final BoardRepository boardRepository;

    public void addBoardMember(Board board, User invitee, BoardRole role) {
        memberRepository.save(
                BoardMember.builder()
                        .user(invitee)
                        .board(board)
                        .role(role)
                        .build()
        );
    }

    public void changeBoardMemberRole(BoardRoleChangeRequest request) {
        if (boardRepository.findById(request.boardId()).isEmpty()) {
            throw new BoardNotFoundException("Board not found");
        }

        BoardMember member = getMemberById(request.targetMemberId());
        member.setRole(request.newRole());
        memberRepository.save(member);
    }

    public void removeMember(BoardMemberRemoveRequest request) {
        BoardMember member = getMemberById(request.targetMemberId());
        memberRepository.delete(member);
    }

    private BoardMember getMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
