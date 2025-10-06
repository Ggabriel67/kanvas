package io.github.ggabriel67.kanvas.task.assignee;

import io.github.ggabriel67.kanvas.column.Column;
import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.exception.AssigneeNotFoundException;
import io.github.ggabriel67.kanvas.exception.MemberAlreadyAssignedException;
import io.github.ggabriel67.kanvas.exception.TaskNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.TaskEventProducer;
import io.github.ggabriel67.kanvas.task.Task;
import io.github.ggabriel67.kanvas.task.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskAssigneeService Unit Tests")
class TaskAssigneeServiceTest
{
    @Mock private TaskAssigneeRepository taskAssigneeRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private TaskEventProducer taskEventProducer;

    @InjectMocks
    private TaskAssigneeService taskAssigneeService;

    @Nested
    class AssignTaskTests {

        @Test
        void shouldAssignTaskAndSendEvent() {
            Integer taskId = 10;
            Integer memberId = 5;
            Integer userId = 2;
            String boardName = "Test Board";

            AssignmentRequest request = new AssignmentRequest(taskId, memberId, userId, boardName);

            Column column = Column.builder().id(1).boardId(100).build();
            Task task = Task.builder().id(taskId).column(column).title("Test Task").build();

            when(taskAssigneeRepository.findByBoardMemberIdAndTaskId(memberId, taskId)).thenReturn(Optional.empty());
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            taskAssigneeService.assignTask(request);

            verify(taskAssigneeRepository).save(argThat(assignee ->
                    assignee.getTask().equals(task) &&
                            assignee.getBoardMemberId().equals(memberId)
            ));

            verify(taskEventProducer).sendTaskAssigned(argThat(event ->
                    event.taskId().equals(taskId) &&
                            event.boardId().equals(100) &&
                            event.boardMemberId().equals(memberId) &&
                            event.userId().equals(userId) &&
                            event.boardName().equals(boardName) &&
                            event.taskTitle().equals("Test Task") &&
                            event.assigned()
            ));
        }

        @Test
        void shouldThrow_WhenMemberAlreadyAssigned() {
            Integer taskId = 10;
            Integer memberId = 5;
            AssignmentRequest request = new AssignmentRequest(taskId, memberId, 2, "Board");

            when(taskAssigneeRepository.findByBoardMemberIdAndTaskId(memberId, taskId))
                    .thenReturn(Optional.of(TaskAssignee.builder().build()));

            assertThatThrownBy(() -> taskAssigneeService.assignTask(request))
                    .isInstanceOf(MemberAlreadyAssignedException.class)
                    .hasMessageContaining("User already assigned");

            verify(taskRepository, never()).findById(any());
            verify(taskAssigneeRepository, never()).save(any());
            verify(taskEventProducer, never()).sendTaskAssigned(any());
        }

        @Test
        void shouldThrow_WhenTaskNotFound() {
            Integer taskId = 10;
            Integer memberId = 5;
            AssignmentRequest request = new AssignmentRequest(taskId, memberId, 2, "Board");

            when(taskAssigneeRepository.findByBoardMemberIdAndTaskId(memberId, taskId))
                    .thenReturn(Optional.empty());
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskAssigneeService.assignTask(request))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("Task not found");

            verify(taskAssigneeRepository, never()).save(any());
            verify(taskEventProducer, never()).sendTaskAssigned(any());
        }
    }

    @Nested
    class UnassignTaskTests {

        @Test
        void shouldUnassignTaskAndSendEvent() {
            Integer taskId = 10;
            Integer memberId = 5;
            Integer userId = 2;
            String boardName = "Board";

            AssignmentRequest request = new AssignmentRequest(taskId, memberId, userId, boardName);

            Column column = Column.builder().id(1).boardId(100).build();
            Task task = Task.builder().id(taskId).column(column).title("Test Task").build();
            TaskAssignee assignee = TaskAssignee.builder().task(task).boardMemberId(memberId).build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskAssigneeRepository.findByBoardMemberIdAndTaskId(memberId, taskId))
                    .thenReturn(Optional.of(assignee));

            taskAssigneeService.unassignTask(request);

            verify(taskAssigneeRepository).delete(assignee);
            verify(taskEventProducer).sendTaskUnassigned(argThat(event ->
                    event.taskId().equals(taskId) &&
                            event.boardId().equals(100) &&
                            event.boardMemberId().equals(memberId) &&
                            event.userId().equals(userId) &&
                            event.boardName().equals(boardName) &&
                            event.taskTitle().equals("Test Task") &&
                            !event.assigned()
            ));
        }

        @Test
        void shouldThrow_WhenTaskNotFound() {
            Integer taskId = 10;
            Integer memberId = 5;
            AssignmentRequest request = new AssignmentRequest(taskId, memberId, 2, "Board");

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskAssigneeService.unassignTask(request))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("Task not found");

            verify(taskAssigneeRepository, never()).findByBoardMemberIdAndTaskId(any(), any());
            verify(taskAssigneeRepository, never()).delete(any());
            verify(taskEventProducer, never()).sendTaskUnassigned(any());
        }

        @Test
        void shouldThrow_WhenAssigneeNotFound() {
            Integer taskId = 10;
            Integer memberId = 5;
            AssignmentRequest request = new AssignmentRequest(taskId, memberId, 2, "Board");

            Task task = Task.builder().id(taskId).column(Column.builder().id(1).boardId(100).build()).title("Test Task").build();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskAssigneeRepository.findByBoardMemberIdAndTaskId(memberId, taskId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskAssigneeService.unassignTask(request))
                    .isInstanceOf(AssigneeNotFoundException.class)
                    .hasMessageContaining("User is not assigned");

            verify(taskAssigneeRepository, never()).delete(any());
            verify(taskEventProducer, never()).sendTaskUnassigned(any());
        }
    }

    @Test
    void deleteMemberAssignments_ShouldDeleteAssignmentsForMember() {
        Integer memberId = 5;
        BoardMemberRemoved memberRemoved = new BoardMemberRemoved(memberId, 1, 10, "Board");

        taskAssigneeService.deleteMemberAssignments(memberRemoved);

        verify(taskAssigneeRepository).deleteByBoardMemberId(memberId);
    }
}