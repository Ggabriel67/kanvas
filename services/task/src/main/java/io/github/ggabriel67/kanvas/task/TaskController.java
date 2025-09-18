package io.github.ggabriel67.kanvas.task;

import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeService;
import io.github.ggabriel67.kanvas.task.assignee.AssignmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController
{
    private final TaskService taskService;
    private final TaskAssigneeService taskAssigneeService;

    @PostMapping
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PatchMapping("/move")
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<TaskResponse> moveTask(@RequestBody MoveTaskRequest request) {
        return ResponseEntity.ok(taskService.moveTask(request));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<Void> deleteTask(@PathVariable("taskId") Integer taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<TaskResponse> updateTask(@RequestBody @Valid TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(request));
    }

    @PostMapping("/assignees")
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<Void> assignTask(@RequestBody AssignmentRequest request) {
        taskAssigneeService.assignTask(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/assignees")
    @PreAuthorize("@boardAuth.canEdit()")
    public ResponseEntity<Void> unassignTask(@RequestBody AssignmentRequest request) {
        taskAssigneeService.unassignTask(request);
        return ResponseEntity.ok().build();
    }
}
