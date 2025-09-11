package io.github.ggabriel67.kanvas.board;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController
{
    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<Void> createBoard(@RequestBody BoardRequest request) {
        boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
