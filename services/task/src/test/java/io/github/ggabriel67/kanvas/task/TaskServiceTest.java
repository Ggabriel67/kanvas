package io.github.ggabriel67.kanvas.task;

import io.github.ggabriel67.kanvas.column.Column;
import io.github.ggabriel67.kanvas.column.ColumnRepository;
import io.github.ggabriel67.kanvas.event.task.TaskCreated;
import io.github.ggabriel67.kanvas.exception.ColumnNotFoundException;
import io.github.ggabriel67.kanvas.exception.TaskNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.TaskEventProducer;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest
{
    @Mock private TaskRepository taskRepository;
    @Mock private ColumnRepository columnRepository;
    @Mock private TaskAssigneeRepository taskAssigneeRepository;
    @Mock private TaskMapper taskMapper;
    @Mock private TaskEventProducer taskEventProducer;

    @InjectMocks
    private TaskService taskService;

    @Nested
    class CreateTaskTests {

        @Test
        void shouldCreateTask_WhenColumnExistsAndHasExistingTasks() {
            Integer columnId = 10;
            Column column = Column.builder().id(columnId).boardId(99).build();
            TaskRequest request = new TaskRequest(columnId, "Test Task");

            when(columnRepository.findById(columnId)).thenReturn(Optional.of(column));
            when(taskRepository.findMaxOrderIndexByColumn(column)).thenReturn(5.0);
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(1);
                return task;
            });
            ReflectionTestUtils.setField(taskService, "step", 100.0);

            TaskResponse response = taskService.createTask(request);

            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(taskCaptor.capture());
            Task savedTask = taskCaptor.getValue();

            assertThat(savedTask.getColumn()).isEqualTo(column);
            assertThat(savedTask.getOrderIndex()).isEqualTo(5.0 + taskService.getStep());
            assertThat(savedTask.getTitle()).isEqualTo("Test Task");
            assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.ACTIVE);

            verify(taskEventProducer).sendTaskCreated(argThat(event ->
                    event.boardId().equals(99)
                            && event.columnId().equals(columnId)
                            && event.orderIndex() == (5.0 + taskService.getStep())
                            && event.title().equals("Test Task")
            ));

            assertThat(response.taskId()).isEqualTo(1);
            assertThat(response.columnId()).isEqualTo(columnId);
            assertThat(response.orderIndex()).isEqualTo(5.0 + taskService.getStep());
            assertThat(response.isExpired()).isFalse();
        }

        @Test
        void shouldCreateTask_WhenNoExistingTasks() {
            Integer columnId = 20;
            Column column = Column.builder().id(columnId).boardId(50).build();
            TaskRequest request = new TaskRequest(columnId, "First Task");

            when(columnRepository.findById(columnId)).thenReturn(Optional.of(column));
            when(taskRepository.findMaxOrderIndexByColumn(column)).thenReturn(null);
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(2);
                return task;
            });
            ReflectionTestUtils.setField(taskService, "step", 100.0);

            TaskResponse response = taskService.createTask(request);

            verify(taskRepository).save(any(Task.class));
            verify(taskEventProducer).sendTaskCreated(any(TaskCreated.class));

            assertThat(response.taskId()).isEqualTo(2);
            assertThat(response.orderIndex()).isEqualTo(taskService.getStep());
        }

        @Test
        void shouldThrowException_WhenColumnDoesNotExist() {
            Integer columnId = 99;
            TaskRequest request = new TaskRequest(columnId, "Orphan Task");
            when(columnRepository.findById(columnId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.createTask(request))
                    .isInstanceOf(ColumnNotFoundException.class)
                    .hasMessageContaining("Column not found");

            verify(taskRepository, never()).save(any());
            verify(taskEventProducer, never()).sendTaskCreated(any());
        }
    }

    @Nested
    class UpdateTaskTests {
        @Test
        void shouldUpdateTaskFieldsAndSendEvent_WhenTaskExists() {
            Integer taskId = 1;
            Column column = Column.builder().id(10).boardId(100).build();
            Task existingTask = Task.builder().id(taskId).title("Old Title").status(TaskStatus.ACTIVE)
                    .priority(TaskPriority.MEDIUM).orderIndex(10.0).column(column).build();

            TaskUpdateRequest request = new TaskUpdateRequest(taskId, "New Title", "New Description",
                    Instant.parse("2025-12-31T00:00:00Z"), TaskPriority.HIGH, TaskStatus.DONE
            );

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

            TaskResponse response = taskService.updateTask(request);

            verify(taskRepository).save(existingTask);
            verify(taskEventProducer).sendTaskUpdated(argThat(event ->
                    event.boardId().equals(100)
                            && event.taskId().equals(taskId)
                            && event.title().equals("New Title")
                            && event.deadline().equals(Instant.parse("2025-12-31T00:00:00Z"))
                            && event.priority().equals("HIGH")
                            && event.taskStatus().equals("DONE")
            ));

            assertThat(existingTask.getTitle()).isEqualTo("New Title");
            assertThat(existingTask.getPriority()).isEqualTo(TaskPriority.HIGH);
            assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.DONE);
            assertThat(existingTask.getDeadline()).isEqualTo(Instant.parse("2025-12-31T00:00:00Z"));

            assertThat(response.taskId()).isEqualTo(taskId);
            assertThat(response.columnId()).isEqualTo(column.getId());
            assertThat(response.orderIndex()).isEqualTo(10.0);
            assertThat(response.isExpired()).isFalse();
        }

        @Test
        void shouldThrowException_WhenTaskNotFound() {
            Integer taskId = 999;
            TaskUpdateRequest request = new TaskUpdateRequest(taskId, "Some Title", null, null, null, null
            );

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.updateTask(request))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("Task not found");

            verify(taskRepository, never()).save(any());
            verify(taskEventProducer, never()).sendTaskUpdated(any());
        }

        @Test
        void shouldOnlyUpdateNonNullOrNonBlankFields() {
            Integer taskId = 55;
            Column column = Column.builder().id(20).boardId(200).build();
            Task existingTask = Task.builder().id(taskId).title("Keep This").description("Old description")
                    .status(TaskStatus.ACTIVE).priority(TaskPriority.LOW).column(column).orderIndex(1.0).build();

            TaskUpdateRequest request = new TaskUpdateRequest(taskId, "", null, null, TaskPriority.HIGH, TaskStatus.DONE);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

            taskService.updateTask(request);

            assertThat(existingTask.getTitle()).isEqualTo("Keep This");
            assertThat(existingTask.getPriority()).isEqualTo(TaskPriority.HIGH);
            assertThat(existingTask.getStatus()).isEqualTo(TaskStatus.DONE);
        }
    }

    @Nested
    class MoveTaskTests {

        @Test
        void shouldMoveTaskToStartOfColumn_WhenNoPrecedingTask() {
            Integer taskId = 1;
            Integer targetColumnId = 10;

            Column targetColumn = Column.builder().id(targetColumnId).boardId(100).build();
            Task followingTask = Task.builder().id(2).orderIndex(20.0).build();
            Task movingTask = Task.builder().id(taskId).orderIndex(50.0).column(Column.builder().id(5).build()).build();

            MoveTaskRequest request = new MoveTaskRequest(targetColumnId, taskId, null, 2);

            when(columnRepository.findById(targetColumnId)).thenReturn(Optional.of(targetColumn));
            when(taskRepository.findById(2)).thenReturn(Optional.of(followingTask));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(movingTask));
            ReflectionTestUtils.setField(taskService, "step", 100.0);

            TaskResponse response = taskService.moveTask(request);

            double expectedOrderIndex = followingTask.getOrderIndex() - taskService.getStep();
            verify(taskRepository).save(movingTask);
            verify(taskEventProducer).sendTaskMoved(argThat(event ->
                    event.boardId().equals(100)
                            && event.beforeColumnId().equals(5)
                            && event.targetColumnId().equals(10)
                            && event.taskId().equals(taskId)
                            && event.newOrderIndex() == expectedOrderIndex
            ));

            assertThat(movingTask.getOrderIndex()).isEqualTo(expectedOrderIndex);
            assertThat(movingTask.getColumn()).isEqualTo(targetColumn);
            assertThat(response.columnId()).isEqualTo(targetColumnId);
            assertThat(response.orderIndex()).isEqualTo(expectedOrderIndex);
        }

        @Test
        void shouldMoveTaskToEndOfColumn_WhenNoFollowingTask() {
            Integer taskId = 1;
            Integer targetColumnId = 10;

            Column targetColumn = Column.builder().id(targetColumnId).boardId(100).build();
            Task precedingTask = Task.builder().id(3).orderIndex(30.0).build();
            Task movingTask = Task.builder().id(taskId).orderIndex(15.0).column(Column.builder().id(8).build()).build();

            MoveTaskRequest request = new MoveTaskRequest(targetColumnId, taskId, 3, null);

            when(columnRepository.findById(targetColumnId)).thenReturn(Optional.of(targetColumn));
            when(taskRepository.findById(3)).thenReturn(Optional.of(precedingTask));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(movingTask));
            ReflectionTestUtils.setField(taskService, "step", 100.0);

            TaskResponse response = taskService.moveTask(request);

            double expectedOrderIndex = precedingTask.getOrderIndex() + taskService.getStep();

            verify(taskRepository).save(movingTask);
            verify(taskEventProducer).sendTaskMoved(argThat(event ->
                    event.targetColumnId().equals(targetColumnId)
                            && event.newOrderIndex() == expectedOrderIndex
            ));
            assertThat(movingTask.getOrderIndex()).isEqualTo(expectedOrderIndex);
            assertThat(movingTask.getColumn()).isEqualTo(targetColumn);
            assertThat(response.orderIndex()).isEqualTo(expectedOrderIndex);
        }

        @Test
        void shouldMoveTaskBetweenTwoTasks() {
            Integer taskId = 1;
            Integer targetColumnId = 7;
            Column targetColumn = Column.builder().id(targetColumnId).boardId(200).build();

            Task preceding = Task.builder().id(10).orderIndex(10.0).build();
            Task following = Task.builder().id(11).orderIndex(20.0).build();
            Task moving = Task.builder().id(taskId).orderIndex(5.0)
                    .column(Column.builder().id(9).build())
                    .build();

            MoveTaskRequest request = new MoveTaskRequest(
                    targetColumnId, taskId, 10, 11
            );

            when(columnRepository.findById(targetColumnId)).thenReturn(Optional.of(targetColumn));
            when(taskRepository.findById(10)).thenReturn(Optional.of(preceding));
            when(taskRepository.findById(11)).thenReturn(Optional.of(following));
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(moving));
            ReflectionTestUtils.setField(taskService, "step", 100.0);

            TaskResponse response = taskService.moveTask(request);

            double expectedOrderIndex = (preceding.getOrderIndex() + following.getOrderIndex()) / 2;

            verify(taskRepository).save(moving);
            verify(taskEventProducer).sendTaskMoved(argThat(event ->
                    event.boardId().equals(200)
                            && event.beforeColumnId().equals(9)
                            && event.targetColumnId().equals(7)
                            && event.newOrderIndex() == expectedOrderIndex
            ));

            assertThat(moving.getOrderIndex()).isEqualTo(expectedOrderIndex);
            assertThat(moving.getColumn()).isEqualTo(targetColumn);
            assertThat(response.orderIndex()).isEqualTo(expectedOrderIndex);
        }

        @Test
        void shouldThrow_WhenTargetColumnDoesNotExist() {
            MoveTaskRequest request = new MoveTaskRequest(999, 1, 10, 12);
            when(columnRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.moveTask(request))
                    .isInstanceOf(ColumnNotFoundException.class)
                    .hasMessageContaining("Column not found");

            verify(taskRepository, never()).save(any());
            verify(taskEventProducer, never()).sendTaskMoved(any());
        }
    }

    @Nested
    class DeleteTaskTests {

        @Test
        void shouldDeleteTaskAndSendEvent() {
            Integer taskId = 42;
            Column column = Column.builder().id(10).boardId(100).build();
            Task task = Task.builder().id(taskId).column(column).build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            taskService.deleteTask(taskId);

            verify(taskAssigneeRepository).deleteAllByTaskId(taskId);
            verify(taskRepository).deleteById(taskId);

            verify(taskEventProducer).sendTaskDeleted(argThat(event ->
                    event.boardId().equals(100)
                            && event.taskId().equals(taskId)
            ));
        }

        @Test
        void shouldThrow_WhenTaskNotFound() {
            when(taskRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deleteTask(999))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("Task not found");

            verify(taskAssigneeRepository, never()).deleteAllByTaskId(any());
            verify(taskRepository, never()).deleteById(any());
            verify(taskEventProducer, never()).sendTaskDeleted(any());
        }
    }

    @Nested
    class GetTaskTests {

        @Test
        void shouldReturnMappedTaskDto() {
            Integer taskId = 10;
            Column column = Column.builder().id(5).boardId(100).build();
            Task task = Task.builder().id(taskId).column(column).orderIndex(1.0).title("Test task").description("Description")
                    .priority(TaskPriority.HIGH).status(TaskStatus.ACTIVE).deadline(Instant.now().plus(2, ChronoUnit.DAYS)).build();

            List<Integer> assigneeIds = List.of(1, 2, 3);
            TaskDto expectedDto = new TaskDto(
                    10, 1.0, 5, "Test task", "Description",
                    null, task.getDeadline(), TaskStatus.ACTIVE, TaskPriority.HIGH,
                    assigneeIds, false
            );

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskAssigneeRepository.findTaskAssigneeIdsByTaskId(taskId)).thenReturn(assigneeIds);
            when(taskMapper.toTaskDto(task, assigneeIds)).thenReturn(expectedDto);

            TaskDto result = taskService.getTask(taskId);

            assertThat(result).isEqualTo(expectedDto);
            verify(taskRepository).findById(taskId);
            verify(taskAssigneeRepository).findTaskAssigneeIdsByTaskId(taskId);
            verify(taskMapper).toTaskDto(task, assigneeIds);
        }

        @Test
        void shouldThrow_WhenTaskNotFound() {
            Integer taskId = 999;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.getTask(taskId))
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining("Task not found");

            verify(taskAssigneeRepository, never()).findTaskAssigneeIdsByTaskId(any());
            verify(taskMapper, never()).toTaskDto(any(), any());
        }
    }

    @Nested
    class IsTaskExpiredTests {
        @Test
        void shouldReturnFalse_WhenStatusDone() {
            boolean expired = taskService.isTaskExpired(TaskStatus.DONE, Instant.now().minus(1, ChronoUnit.DAYS));
            assertThat(expired).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenDeadlineIsNull() {
            boolean expired = taskService.isTaskExpired(TaskStatus.ACTIVE, null);
            assertThat(expired).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenDeadlineIsInFuture() {
            boolean expired = taskService.isTaskExpired(TaskStatus.ACTIVE, Instant.now().plus(1, ChronoUnit.DAYS));
            assertThat(expired).isFalse();
        }

        @Test
        void shouldReturnTrue_WhenDeadlineIsPastAndStatusNotDone() {
            boolean expired = taskService.isTaskExpired(TaskStatus.ACTIVE, Instant.now().minus(1, ChronoUnit.DAYS));
            assertThat(expired).isTrue();
        }
    }
}
