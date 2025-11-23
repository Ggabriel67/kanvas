package io.github.ggabriel67.kanvas.board.member;

import io.github.ggabriel67.kanvas.authorization.board.BoardAuthorization;
import io.github.ggabriel67.kanvas.authorization.board.BoardRole;
import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardRepository;
import io.github.ggabriel67.kanvas.board.BoardVisibility;
import io.github.ggabriel67.kanvas.board.invitation.BoardInvitationRepository;
import io.github.ggabriel67.kanvas.event.board.BoardMemberJoined;
import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.event.board.RoleChanged;
import io.github.ggabriel67.kanvas.exception.BoardNotFoundException;
import io.github.ggabriel67.kanvas.exception.BoardRoleNotFoundException;
import io.github.ggabriel67.kanvas.exception.UserNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.BoardEventProducer;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMember;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardMemberService
{
    private final BoardMemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final BoardEventProducer boardEventProducer;
    private final BoardAuthorization boardAuthorization;
    private final BoardInvitationRepository boardInvitationRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardMemberRepository boardMemberRepository;

    public void addBoardMember(Board board, User invitee, BoardRole role) {
        BoardMember member = memberRepository.save(
                BoardMember.builder()
                        .user(invitee)
                        .board(board)
                        .role(role)
                        .build()
        );

        boardEventProducer.sendMemberJoined(new BoardMemberJoined(
                board.getId(), member.getId(), invitee.getId(), invitee.getFirstname(), invitee.getLastname(), invitee.getUsername(),
                invitee.getAvatarColor(), member.getRole().name(), member.getJoinedAt())
        );
    }

    public void changeMemberRole(BoardRoleChangeRequest request) {
        if (boardRepository.findById(request.boardId()).isEmpty()) {
            throw new BoardNotFoundException("Board not found");
        }

        BoardMember member = getMemberById(request.targetMemberId());
        member.setRole(request.newRole());
        memberRepository.save(member);

        boardEventProducer.sendRoleChanged(new RoleChanged(
                member.getBoard().getId(), member.getId(), request.newRole().name()
        ));
    }

    @Transactional
    public void removeMember(BoardMemberRemoveRequest request) {
        if (boardRepository.findById(request.boardId()).isEmpty()) {
            throw new BoardNotFoundException("Board not found");
        }

        BoardMember member = getMemberById(request.targetMemberId());
        BoardMemberRemoved memberRemoved = new BoardMemberRemoved(
                member.getId(), member.getUser().getId(), member.getBoard().getId(), member.getBoard().getName()
        );
        boardInvitationRepository.deleteAllByInvitee(member.getUser());
        memberRepository.delete(member);

        boardEventProducer.sendMemberRemoved(memberRemoved);
    }

    private BoardMember getMemberById(Integer memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public String getBoardRole(Integer boardId) {
        Integer userId = boardAuthorization.getCurrentUserId();

        BoardRole role = memberRepository.findRoleByUserIdAndBoardId(userId, boardId)
                .orElse(null);

        if (role == null) {
            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new BoardNotFoundException("Board not found"));

            WorkspaceMember workspaceMember = workspaceMemberRepository.findByUserIdAndWorkspaceId(userId, board.getWorkspace().getId())
                    .orElse(null);

            boolean canView = (board.getVisibility() == BoardVisibility.WORKSPACE_PUBLIC && workspaceMember != null);

            if (!canView) {
                throw new BoardRoleNotFoundException("User has no permissions in this board");
            }
            return BoardRole.VIEWER.name();
        }

        return role.name();
    }

    public void leaveBoard(BoardMemberRemoveRequest request) {
        BoardMember member = getMemberById(request.targetMemberId());

        BoardMemberRemoved memberRemoved = new BoardMemberRemoved(
                member.getId(), member.getUser().getId(), member.getBoard().getId(), null
        );

        boardEventProducer.sendMemberLeft(memberRemoved);

        memberRepository.delete(member);
    }

    public List<MemberDto> getBoardMembers(Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        return boardMemberRepository.findMembersWithWorkspaceRole(board, board.getWorkspace());
    }
}
