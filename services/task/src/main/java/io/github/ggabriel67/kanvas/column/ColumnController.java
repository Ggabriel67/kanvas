package io.github.ggabriel67.kanvas.column;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/columns")
@RequiredArgsConstructor
public class ColumnController
{
    private final ColumnService columnService;

    @PostMapping
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<ColumnResponse> createColumn(@RequestBody @Valid ColumnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(columnService.createColumn(request));
    }

    @PatchMapping("/{columnId}")
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<Void> updateColumnName(@PathVariable("columnId") Integer columnId, @RequestBody @Valid ColumnRequest request) {
        columnService.updateColumnName(columnId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/move")
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<ColumnResponse> moveColumn(@RequestBody MoveColumnRequest request) {
        return ResponseEntity.ok(columnService.moveColumn(request));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<List<ColumnDto>> getAllBoardColumns(@PathVariable("boardId") Integer boardId) {
        return ResponseEntity.ok(columnService.getAllBoardColumns(boardId));
    }

    @DeleteMapping("/{columnId}")
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<Void> deleteColumn(@PathVariable("columnId") Integer columnId) {
        columnService.deleteColumn(columnId);
        return ResponseEntity.ok().build();
    }
}
