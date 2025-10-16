package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.board.member.BoardMemberRemoveRequest;
import io.github.ggabriel67.kanvas.board.member.BoardMemberService;
import io.github.ggabriel67.kanvas.board.member.BoardRoleChangeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController
{
    private final BoardService boardService;
    private final BoardMemberService boardMemberService;

    @PostMapping
    @PreAuthorize("@workspaceAuth.isMember(#request.workspaceId())")
    public ResponseEntity<Integer> createBoard(@RequestBody @Valid BoardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(request));
    }

    @GetMapping("/{boardId}")
    @PreAuthorize("@boardAuth.canView(#boardId)")
    public ResponseEntity<BoardDto> getBoard(@PathVariable("boardId") Integer boardId) {
        return ResponseEntity.ok(boardService.getBoard(boardId));
    }

    @PatchMapping("/{boardId}")
    @PreAuthorize("@boardAuth.isAdmin(#boardId)")
    public ResponseEntity<Void> updateBoard(@RequestBody BoardUpdateRequest request, @PathVariable("boardId") Integer boardId) {
        boardService.updateBoard(request, boardId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/members")
    @PreAuthorize("@boardAuth.canModerate(#request.boardId(), #request.targetMemberId())")
    public ResponseEntity<Void> changeBoardMemberRole(@RequestBody BoardRoleChangeRequest request) {
        boardMemberService.changeMemberRole(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/members")
    @PreAuthorize("@boardAuth.canModerate(#request.boardId(), #request.targetMemberId())")
    public ResponseEntity<Void> removeBoardMember(@RequestBody BoardMemberRemoveRequest request) {
        boardMemberService.removeMember(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{boardId}")
    @PreAuthorize("@boardAuth.canDelete(#boardId)")
    public ResponseEntity<Void> deleteBoard(@PathVariable("boardId") Integer boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{boardId}/roles")
    public ResponseEntity<String> getBoardRole(@PathVariable("boardId") Integer boardId) {
        return ResponseEntity.ok(boardMemberService.getBoardRole(boardId));
    }

    @DeleteMapping("/leave")
    @PreAuthorize("@boardAuth.isMember(#request.boardId())")
    public ResponseEntity<Void> leaveBoard(@RequestBody BoardMemberRemoveRequest request) {
        boardMemberService.leaveBoard(request);
        return ResponseEntity.ok().build();
    }
}
