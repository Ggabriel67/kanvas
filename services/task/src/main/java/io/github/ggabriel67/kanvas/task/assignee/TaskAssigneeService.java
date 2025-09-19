package io.github.ggabriel67.kanvas.task.assignee;

import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.event.task.TaskAssignment;
import io.github.ggabriel67.kanvas.exception.AssigneeNotFoundException;
import io.github.ggabriel67.kanvas.exception.MemberAlreadyAssignedException;
import io.github.ggabriel67.kanvas.exception.TaskNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.TaskEventProducer;
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
    private final TaskEventProducer taskEventProducer;

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

        taskEventProducer.sendTaskAssigned(new TaskAssignment(task.getColumn().getBoardId(),
                task.getId(), request.memberId(), request.userId(), task.getTitle(), request.boardName())
        );
    }

    public void unassignTask(AssignmentRequest request) {
        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        TaskAssignee assignee = taskAssigneeRepository.findByBoardMemberIdAndTaskId(request.memberId(), request.taskId())
                .orElseThrow(() -> new AssigneeNotFoundException("User is not assigned"));

        TaskAssignment taskAssignment = new TaskAssignment(task.getColumn().getBoardId(), request.taskId(),
                request.memberId(), request.userId(), task.getTitle(), request.boardName());

        taskAssigneeRepository.delete(assignee);

        taskEventProducer.sendTaskUnassigned(taskAssignment);
    }

    public void deleteMemberAssignments(BoardMemberRemoved memberRemoved) {
        taskAssigneeRepository.deleteByBoardMemberId(memberRemoved.memberId());
    }
}
