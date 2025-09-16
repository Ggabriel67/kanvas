package io.github.ggabriel67.kanvas.task;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController
{
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PatchMapping("/move")
    public ResponseEntity<TaskResponse> moveTask(@RequestBody MoveTaskRequest request) {
        return ResponseEntity.ok(taskService.moveTask(request));
    }
}
