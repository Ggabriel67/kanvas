package io.github.ggabriel67.kanvas.workspace;

import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberRequest;
import io.github.ggabriel67.kanvas.workspace.member.WorkspaceMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController
{
    private final WorkspaceService workspaceService;
    private final WorkspaceMemberService memberService;

    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody WorkspaceRequest request) {
        workspaceService.createWorkspace(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping
    public ResponseEntity<Void> updateWorkspace(@RequestBody WorkspaceRequest request) {
        workspaceService.updateWorkspace(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/members")
    public ResponseEntity<Void> addWorkspaceMember(@RequestBody WorkspaceMemberRequest request) {
        memberService.addWorkspaceMember(request);
        return ResponseEntity.accepted().build();
    }
}
