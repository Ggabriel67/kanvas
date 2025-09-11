package io.github.ggabriel67.kanvas.invitation;

import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationRequest;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController
{
    private final WorkspaceInvitationService workspaceInvitationService;

    @PostMapping("/workspaces")
    @PreAuthorize("@workspaceAuth.isAdminOrOwner(#request.workspaceId())")
    public ResponseEntity<Void> sendWorkspaceInvitation(@RequestBody WorkspaceInvitationRequest request) {
        workspaceInvitationService.sendInvitation(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/workspaces/{invitationId}/accept")
    public ResponseEntity<Void> acceptWorkspaceInvitation(@PathVariable("invitationId") Integer invitationId) {
        workspaceInvitationService.acceptInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/workspaces/{invitationId}/decline")
    public ResponseEntity<Void> declineWorkspaceInvitation(@PathVariable("invitationId") Integer invitationId) {
        workspaceInvitationService.declineInvitation(invitationId);
        return ResponseEntity.ok().build();
    }
}
