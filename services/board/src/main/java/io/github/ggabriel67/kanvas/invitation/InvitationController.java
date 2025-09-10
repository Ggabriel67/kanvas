package io.github.ggabriel67.kanvas.invitation;

import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationRepository;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationRequest;
import io.github.ggabriel67.kanvas.workspace.invitation.WorkspaceInvitationService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController
{
    private final WorkspaceInvitationService workspaceInvitationService;

    @PostMapping("/workspaces")
    public ResponseEntity<Void> sendWorkspaceInvitation(@RequestBody WorkspaceInvitationRequest request) {
        workspaceInvitationService.sendInvitation(request);
        return ResponseEntity.ok().build();
    }
}
