package io.github.ggabriel67.kanvas.task.assignee;

import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.exception.AssigneeNotFoundException;
import io.github.ggabriel67.kanvas.exception.MemberAlreadyAssignedException;
import io.github.ggabriel67.kanvas.exception.TaskNotFoundException;
import io.github.ggabriel67.kanvas.task.Task;
import io.github.ggabriel67.kanvas.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAssigneeService
{
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskRepository taskRepository;

    public void assignTask(AssignmentRequest request) {
        if (taskAssigneeRepository.findByBoardMemberIdAndTaskId(
                request.memberId(), request.taskId()).isPresent()) {
            throw new MemberAlreadyAssignedException("User already assigned");
        }
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        taskAssigneeRepository.save(
                TaskAssignee.builder()
                        .task(task)
                        .boardMemberId(request.memberId())
                        .build()
        );
    }

    public void unassignTask(AssignmentRequest request) {
        TaskAssignee assignee = taskAssigneeRepository.findByBoardMemberIdAndTaskId(request.memberId(), request.boardId())
                .orElseThrow(() -> new AssigneeNotFoundException("User is not assigned"));

        taskAssigneeRepository.delete(assignee);
    }

    public void deleteMemberAssignments(BoardMemberRemoved memberRemoved) {
        taskAssigneeRepository.deleteByBoardMemberId(memberRemoved.memberId());
    }
}
