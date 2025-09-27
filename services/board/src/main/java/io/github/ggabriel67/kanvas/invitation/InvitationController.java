package io.github.ggabriel67.kanvas.invitation;

import io.github.ggabriel67.kanvas.board.invitation.BoardInvitationRequest;
import io.github.ggabriel67.kanvas.board.invitation.BoardInvitationService;
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
    private final BoardInvitationService boardInvitationService;

    @PostMapping("/workspaces")
    @PreAuthorize("@workspaceAuth.isAdminOrOwner(#request.workspaceId())")
    public ResponseEntity<Void> createWorkspaceInvitation(@RequestBody WorkspaceInvitationRequest request) {
        workspaceInvitationService.createInvitation(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/workspaces/{invitationId}/accept")
    public ResponseEntity<Void> acceptWorkspaceInvitation(@PathVariable("invitationId") Integer invitationId) {
        workspaceInvitationService.acceptInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/workspaces/{invitationId}/decline")
    public ResponseEntity<Void> declineWorkspaceInvitation(@PathVariable("invitationId") Integer invitationId) {
        workspaceInvitationService.declineInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/boards")
    @PreAuthorize("@boardAuth.isAdmin(#request.boardId())")
    public ResponseEntity<Void> createBoardInvitation(@RequestBody BoardInvitationRequest request) {
        boardInvitationService.createInvitation(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/boards/{invitationId}/accept")
    public ResponseEntity<Void> acceptBoardInvitation(@PathVariable("invitationId") Integer invitationId) {
        boardInvitationService.acceptInvitation(invitationId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/boards/{invitationId}/decline")
    public ResponseEntity<Void> declineBoardInvitation(@PathVariable("invitationId") Integer invitationId) {
        boardInvitationService.declineInvitation(invitationId);
        return ResponseEntity.ok().build();
    }
}
