package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRemoveRequest;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceRoleChangeRequest;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberDto;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController
{
    private final WorkspaceService workspaceService;
    private final WorkspaceMemberService workspaceMemberService;

    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody WorkspaceRequest request) {
        workspaceService.createWorkspace(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{workspaceId}")
    @PreAuthorize("@workspaceAuth.isAdminOrOwner(#workspaceId)")
    public ResponseEntity<Void> updateWorkspace(@RequestBody WorkspaceRequest request, @PathVariable("workspaceId") Integer workspaceId) {
        workspaceService.updateWorkspace(request, workspaceId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{workspaceId}")
    @PreAuthorize("@workspaceAuth.isMember(#workspaceId)")
    public ResponseEntity<WorkspaceDto> getWorkspace(@PathVariable("workspaceId") Integer workspaceId) {
        return ResponseEntity.ok(workspaceService.getWorkspace(workspaceId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<WorkspaceDtoProjection>> getAllUserWorkspaces(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(workspaceMemberService.getAllUserWorkspaces(userId));
    }

    @GetMapping("/{workspaceId}/members")
    @PreAuthorize("@workspaceAuth.isMember(#workspaceId)")
    public ResponseEntity<List<WorkspaceMemberDto>> getAllWorkspaceMembers(@PathVariable("workspaceId") Integer workspaceId) {
        return ResponseEntity.ok(workspaceMemberService.getAllWorkspaceMembers(workspaceId));
    }

    @PatchMapping("/members")
    @PreAuthorize("@workspaceAuth.canModerate(#request.workspaceId(), #request.targetMemberId())")
    public ResponseEntity<Void> changeWorkspaceMemberRole(@RequestBody WorkspaceRoleChangeRequest request) {
        workspaceMemberService.changeWorkspaceMemberRole(request);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/members")
    @PreAuthorize("@workspaceAuth.canModerate(#request.workspaceId(), #request.targetMemberId())")
    public ResponseEntity<Void> removeWorkspaceMember(@RequestBody WorkspaceMemberRemoveRequest request) {
        workspaceMemberService.removeMember(request);
        return ResponseEntity.accepted().build();
    }
}
