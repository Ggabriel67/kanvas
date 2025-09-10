package io.github.ggabriel67.kanvas.workspace.invitation;

import io.github.ggabriel67.kanvas.exception.*;
import io.github.ggabriel67.kanvas.invitation.InvitationStatus;
import io.github.ggabriel67.kanvas.user.User;
import io.github.ggabriel67.kanvas.user.UserService;
import io.github.ggabriel67.kanvas.workspace.Workspace;
import io.github.ggabriel67.kanvas.workspace.WorkspaceService;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRepository;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceInvitationService
{
    private final WorkspaceInvitationRepository invitationRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserService userService;
    private final WorkspaceService workspaceService;
    private final WorkspaceMemberService memberService;

    public void sendInvitation(WorkspaceInvitationRequest request) {
        if (memberRepository.findByUserIdAndWorkspaceId(request.inviteeId(), request.workspaceId())
                .isPresent()) {
            throw new MemberAlreadyExistsException("This user is already a member of the workspace");
        }

        Optional<WorkspaceInvitation> invitation = invitationRepository.findByInviteeIdAndWorkspaceId(request.inviteeId(), request.workspaceId());
        if (invitation.isPresent()) {
            if (invitation.get().getStatus() == InvitationStatus.PENDING) {
                throw new InvitationPendingException("Invitation already pending");
            }
        }

        User inviter = userService.getUserById(request.inviterId());
        User invitee = userService.getUserById(request.inviteeId());
        Workspace workspace = workspaceService.getWorkspaceById(request.workspaceId());

        invitationRepository.save(
                WorkspaceInvitation.builder()
                        .inviter(inviter)
                        .invitee(invitee)
                        .workspace(workspace)
                        .role(request.role())
                        .status(InvitationStatus.PENDING)
                        .expirationTime(Instant.now().plus(14, ChronoUnit.DAYS))
                        .build()
        );
    }

    public void acceptInvitation(Integer invitationId) {
        WorkspaceInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation does not exist"));

        validate(invitation);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        memberService.addWorkspaceMember(invitation.getWorkspace(), invitation.getInvitee(), invitation.getRole());
    }

    public void declineInvitation(Integer invitationId) {
        WorkspaceInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation does not exist"));

        validate(invitation);
        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }

    private void validate(WorkspaceInvitation invitation) {
        if (invitation.getStatus() != InvitationStatus.PENDING) throw new InvalidStatusException("Invitation is not pending");
        if (invitation.getExpirationTime().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);

            throw new InvitationExpiredException("Invitation has expired");
        }
    }
}
