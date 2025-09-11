package io.github.ggabriel67.kanvas.board.invitation;

import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardService;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.board.member.BoardMemberService;
import io.github.ggabriel67.kanvas.exception.*;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardInvitationService
{
    private final UserService userService;
    private final BoardMemberService memberService;
    private final BoardMemberRepository memberRepository;
    private final BoardInvitationRepository invitationRepository;
    private final BoardService boardService;

    public void sendInvitation(BoardInvitationRequest request) {
        if (memberRepository.findByUserIdAndBoardId(request.inviteeId(), request.boardId())
                .isPresent()) {
            throw new MemberAlreadyExistsException("This user is already a member of the board");
        }

        Optional<BoardInvitation> invitation = invitationRepository.findByInviteeIdAndBoardId(request.inviteeId(), request.boardId());
        if (invitation.isPresent()) {
            if (invitation.get().getStatus() == InvitationStatus.PENDING) {
                throw new InvitationPendingException("Invitation already pending");
            }
        }

        User inviter = userService.getUserById(request.inviterId());
        User invitee = userService.getUserById(request.inviteeId());
        Board board = boardService.getBoardById(request.boardId());

        invitationRepository.save(
                BoardInvitation.builder()
                        .inviter(inviter)
                        .invitee(invitee)
                        .board(board)
                        .role(request.role())
                        .status(InvitationStatus.PENDING)
                        .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                        .build()
        );
    }

    public void acceptInvitation(Integer invitationId) {
        BoardInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation does not exist"));

        validate(invitation);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        memberService.addBoardMember(invitation.getBoard(), invitation.getInvitee(), invitation.getRole());
    }

    public void declineInvitation(Integer invitationId) {
        BoardInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation does not exist"));

        validate(invitation);
        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }

    private void validate(BoardInvitation invitation) {
        if (invitation.getStatus() != InvitationStatus.PENDING) throw new InvalidStatusException("Invitation is not pending");
        if (invitation.getExpirationTime().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);

            throw new InvitationExpiredException("Invitation has expired");
        }
    }
}
