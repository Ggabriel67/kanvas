package io.github.ggabriel67.kanvas.board;

import io.github.ggabriel67.kanvas.board.member.BoardMemberRemoveRequest;
import io.github.ggabriel67.kanvas.board.member.BoardMemberService;
import io.github.ggabriel67.kanvas.board.member.BoardRoleChangeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController
{
    private final BoardService boardService;
    private final BoardMemberService boardMemberService;

    @PostMapping
    public ResponseEntity<Void> createBoard(@RequestBody @Valid BoardRequest request) {
        boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> getBoard(@PathVariable("boardId") Integer boardId) {
        return ResponseEntity.ok(boardService.getBoard(boardId));
    }

    @PatchMapping("/members")
    public ResponseEntity<Void> changeBoardMemberRole(BoardRoleChangeRequest request) {
        boardMemberService.changeBoardMemberRole(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/members")
    public ResponseEntity<Void> removeBoardMember(@RequestBody BoardMemberRemoveRequest request) {
        boardMemberService.removeMember(request);
        return ResponseEntity.ok().build();
    }
}
