package io.github.ggabriel67.kanvas.board.invitation;

import io.github.ggabriel67.kanvas.board.Board;
import io.github.ggabriel67.kanvas.board.BoardService;
import io.github.ggabriel67.kanvas.board.member.BoardMemberRepository;
import io.github.ggabriel67.kanvas.board.member.BoardMemberService;
import io.github.ggabriel67.kanvas.exception.*;
import io.github.ggabriel67.kanvas.invitation.InvitationScope;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.kafka.producer.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.kafka.producer.invitation.InvitationEventProducer;
import io.github.ggabriel67.kanvas.kafka.producer.invitation.InvitationUpdate;
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
    private final InvitationEventProducer invitationEventProducer;

    public void sendInvitation(BoardInvitationRequest request) {
        if (memberRepository.findByUserIdAndBoardId(request.inviteeId(), request.boardId())
                .isPresent()) {
            throw new MemberAlreadyExistsException("This user is already a member of the board");
        }

        Optional<BoardInvitation> existingInvitation = invitationRepository.findByInviteeIdAndBoardId(request.inviteeId(), request.boardId());
        if (existingInvitation.isPresent()) {
            if (existingInvitation.get().getStatus() == InvitationStatus.PENDING) {
                throw new InvitationPendingException("Invitation already pending");
            }
        }

        User inviter = userService.getUserById(request.inviterId());
        User invitee = userService.getUserById(request.inviteeId());
        Board board = boardService.getBoardById(request.boardId());

        BoardInvitation invitation = invitationRepository.save(
                BoardInvitation.builder()
                        .inviter(inviter)
                        .invitee(invitee)
                        .board(board)
                        .role(request.role())
                        .status(InvitationStatus.PENDING)
                        .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                        .build()
        );

        invitationEventProducer.sendInvitationCreated(new InvitationCreated(
                invitation.getId(), invitee.getId(), inviter.getUsername(), board.getName(), InvitationScope.BOARD)
        );
    }

    public void acceptInvitation(Integer invitationId) {
        BoardInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation does not exist"));

        validate(invitation);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        memberService.addBoardMember(invitation.getBoard(), invitation.getInvitee(), invitation.getRole());

        invitationEventProducer.sendInvitationUpdated(new InvitationUpdate(
                invitation.getId(), invitation.getInvitee().getId(), InvitationStatus.ACCEPTED)
        );
    }

    public void declineInvitation(Integer invitationId) {
        BoardInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation does not exist"));

        validate(invitation);
        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);

        invitationEventProducer.sendInvitationUpdated(new InvitationUpdate(
                invitation.getId(), invitation.getInvitee().getId(), InvitationStatus.DECLINED)
        );
    }

    private void validate(BoardInvitation invitation) {
        if (invitation.getStatus() != InvitationStatus.PENDING) throw new InvalidStatusException("Invitation is not pending");
        if (invitation.getExpirationTime().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);

            invitationEventProducer.sendInvitationUpdated(new InvitationUpdate(
                    invitation.getId(), invitation.getInvitee().getId(), InvitationStatus.EXPIRED)
            );

            throw new InvitationExpiredException("Invitation has expired");
        }
    }
}
