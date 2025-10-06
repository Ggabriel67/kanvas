package io.github.ggabriel67.kanvas.column;

import io.github.ggabriel67.kanvas.event.column.ColumnCreated;
import io.github.ggabriel67.kanvas.exception.ColumnNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.ColumnEventProducer;
import io.github.ggabriel67.kanvas.task.TaskRepository;
import io.github.ggabriel67.kanvas.task.TaskService;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ColumnService Unit Tests")
class ColumnServiceTest
{
    @Mock private ColumnRepository columnRepository;
    @Mock private ColumnMapper columnMapper;
    @Mock private TaskService taskService;
    @Mock private TaskRepository taskRepository;
    @Mock private TaskAssigneeRepository taskAssigneeRepository;
    @Mock private ColumnEventProducer columnEventProducer;

    @InjectMocks
    private ColumnService columnService;

    @Nested
    class CreateColumnTests {

        @Test
        void shouldCreateColumn_WhenBoardHasExistingColumns() {
            ColumnRequest request = new ColumnRequest(10, "In Progress");
            Double existingMaxOrderIndex = 5.0;
            Double step = 100.0;

            when(columnRepository.findMaxOrderIndexByBoardId(10)).thenReturn(existingMaxOrderIndex);
            when(columnRepository.save(any(Column.class))).thenAnswer(invocation -> {
                Column c = invocation.getArgument(0);
                c.setId(1);
                return c;
            });
            when(columnMapper.toColumnResponse(any(Column.class)))
                    .thenReturn(new ColumnResponse(1, "In Progress", 105.0));

            ReflectionTestUtils.setField(columnService, "step", step);

            ColumnResponse response = columnService.createColumn(request);

            assertThat(response.columnId()).isEqualTo(1);
            assertThat(response.name()).isEqualTo("In Progress");
            assertThat(response.orderIndex()).isEqualTo(105.0);

            ArgumentCaptor<Column> columnCaptor = ArgumentCaptor.forClass(Column.class);
            verify(columnRepository).save(columnCaptor.capture());
            Column savedColumn = columnCaptor.getValue();
            assertThat(savedColumn.getOrderIndex()).isEqualTo(existingMaxOrderIndex + step);
            assertThat(savedColumn.getBoardId()).isEqualTo(10);
            assertThat(savedColumn.getName()).isEqualTo("In Progress");

            verify(columnEventProducer).sendColumnCreated(argThat(event ->
                    event.columnId().equals(1) &&
                            event.orderIndex() == 105.0 &&
                            event.name().equals("In Progress") &&
                            event.boardId().equals(10)
            ));
        }

        @Test
        void shouldCreateColumn_WhenNoExistingColumns() {
            ColumnRequest request = new ColumnRequest(10, "To Do");
            Double step = 50.0;

            when(columnRepository.findMaxOrderIndexByBoardId(10)).thenReturn(null);
            when(columnRepository.save(any(Column.class))).thenAnswer(invocation -> {
                Column c = invocation.getArgument(0);
                c.setId(5);
                return c;
            });
            when(columnMapper.toColumnResponse(any(Column.class)))
                    .thenReturn(new ColumnResponse(5, "To Do", 10));

            ReflectionTestUtils.setField(columnService, "step", step);

            ColumnResponse response = columnService.createColumn(request);

            assertThat(response.orderIndex() == step);
            assertThat(response.name()).isEqualTo("To Do");

            verify(columnRepository).save(argThat(column ->
                    column.getOrderIndex() == step &&
                            column.getBoardId().equals(10) &&
                            column.getName().equals("To Do")
            ));

            verify(columnEventProducer).sendColumnCreated(any(ColumnCreated.class));
        }
    }

    @Nested
    class UpdateColumnNameTests {
        @Test
        void shouldUpdateColumnNameAndSendEvent() {
            Column column = Column.builder().id(10).boardId(20).name("Old Name").orderIndex(100.0).build();

            ColumnRequest request = new ColumnRequest(20, "New Name");

            when(columnRepository.findById(10)).thenReturn(Optional.of(column));

            columnService.updateColumnName(10, request);

            assertThat(column.getName()).isEqualTo("New Name");

            verify(columnRepository).save(column);
            verify(columnEventProducer).sendColumnUpdated(argThat(event ->
                    event.boardId().equals(20) &&
                            event.columnId().equals(10) &&
                            event.columnName().equals("New Name")
            ));
        }

        @Test
        void shouldThrowException_WhenColumnDoesNotExist() {
            when(columnRepository.findById(999)).thenReturn(Optional.empty());
            ColumnRequest request = new ColumnRequest(10, "New Name");

            assertThatThrownBy(() -> columnService.updateColumnName(999, request))
                    .isInstanceOf(ColumnNotFoundException.class)
                    .hasMessageContaining("Column not found");

            verify(columnRepository, never()).save(any());
            verify(columnEventProducer, never()).sendColumnUpdated(any());
        }
    }
    @Nested
    class MoveColumnTests {

        @Test
        void shouldMoveColumnToFirstPosition() {
            Column following = Column.builder().id(2).orderIndex(200.0).boardId(10).build();
            Column moved = Column.builder().id(1).orderIndex(150.0).boardId(10).build();

            MoveColumnRequest request = new MoveColumnRequest(1, null, 2);

            when(columnRepository.findById(2)).thenReturn(Optional.of(following));
            when(columnRepository.findById(1)).thenReturn(Optional.of(moved));
            ReflectionTestUtils.setField(columnService, "step", 100.0);

            columnService.moveColumn(request);

            double expectedOrderIndex = following.getOrderIndex() - columnService.getStep();
            assertThat(moved.getOrderIndex()).isEqualTo(expectedOrderIndex);

            verify(columnRepository).save(moved);
            verify(columnEventProducer).sendColumnMoved(argThat(event ->
                    event.boardId().equals(10) &&
                            event.columnId().equals(1) &&
                            event.newOrderIndex() == expectedOrderIndex
            ));
        }

        @Test
        void shouldMoveColumnToLastPosition() {
            Column preceding = Column.builder().id(2).orderIndex(300.0).boardId(10).build();
            Column moved = Column.builder().id(1).orderIndex(150.0).boardId(10).build();

            MoveColumnRequest request = new MoveColumnRequest(1, 2, null);

            when(columnRepository.findById(2)).thenReturn(Optional.of(preceding));
            when(columnRepository.findById(1)).thenReturn(Optional.of(moved));
            ReflectionTestUtils.setField(columnService, "step", 100.0);

            columnService.moveColumn(request);

            double expectedOrderIndex = preceding.getOrderIndex() + columnService.getStep();
            assertThat(moved.getOrderIndex()).isEqualTo(expectedOrderIndex);

            verify(columnRepository).save(moved);
            verify(columnEventProducer).sendColumnMoved(argThat(event ->
                    event.boardId().equals(10) &&
                            event.columnId().equals(1) &&
                            event.newOrderIndex() == expectedOrderIndex
            ));
        }

        @Test
        void shouldMoveColumnBetweenTwoColumns() {
            Column preceding = Column.builder().id(2).orderIndex(100.0).boardId(10).build();
            Column following = Column.builder().id(3).orderIndex(300.0).boardId(10).build();
            Column moved = Column.builder().id(1).orderIndex(200.0).boardId(10).build();

            MoveColumnRequest request = new MoveColumnRequest(1, 2, 3);

            when(columnRepository.findById(2)).thenReturn(Optional.of(preceding));
            when(columnRepository.findById(3)).thenReturn(Optional.of(following));
            when(columnRepository.findById(1)).thenReturn(Optional.of(moved));

            columnService.moveColumn(request);

            double expectedOrderIndex = (preceding.getOrderIndex() + following.getOrderIndex()) / 2;
            assertThat(moved.getOrderIndex()).isEqualTo(expectedOrderIndex);

            verify(columnRepository).save(moved);
            verify(columnEventProducer).sendColumnMoved(argThat(event ->
                    event.boardId().equals(10) &&
                            event.columnId().equals(1) &&
                            event.newOrderIndex() == expectedOrderIndex
            ));
        }

        @Test
        void shouldThrowException_WhenColumnNotFound() {
            MoveColumnRequest request = new MoveColumnRequest(1, null, 2);

            when(columnRepository.findById(2)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> columnService.moveColumn(request))
                    .isInstanceOf(ColumnNotFoundException.class)
                    .hasMessageContaining("Column not found");

            verify(columnRepository, never()).save(any());
            verify(columnEventProducer, never()).sendColumnMoved(any());
        }
    }

    @Nested
    class DeleteColumnTests {

        @Test
        void shouldDeleteColumnAndSendKafkaEvent() {
            Column column = Column.builder().id(1).boardId(10).name("In Progress").orderIndex(100.0).build();

            when(columnRepository.findById(1)).thenReturn(Optional.of(column));

            columnService.deleteColumn(1);

            verify(taskAssigneeRepository).deleteAllByColumnId(1);
            verify(taskRepository).deleteAllByColumnId(1);
            verify(columnRepository).delete(column);

            verify(columnEventProducer).sendColumnDeleted(argThat(event ->
                    event.columnId().equals(1) &&
                            event.boardId().equals(10)
            ));

            verifyNoMoreInteractions(columnRepository, columnEventProducer, taskRepository, taskAssigneeRepository);
        }

        @Test
        void shouldThrowException_WhenColumnNotFound() {
            when(columnRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> columnService.deleteColumn(999))
                    .isInstanceOf(ColumnNotFoundException.class)
                    .hasMessageContaining("Column not found");

            verify(taskAssigneeRepository, never()).deleteAllByColumnId(any());
            verify(taskRepository, never()).deleteAllByColumnId(any());
            verify(columnRepository, never()).delete(any());
            verify(columnEventProducer, never()).sendColumnDeleted(any());
        }
    }

    @Nested
    class DeleteAllByBoardIdTests {
        @Test
        void shouldDeleteColumnsAndAssociatedTasks_WhenColumnsExist() {
            Integer boardId = 42;
            List<Column> columns = List.of(
                    Column.builder().id(1).boardId(boardId).build(),
                    Column.builder().id(2).boardId(boardId).build()
            );

            when(columnRepository.findAllByBoardId(boardId)).thenReturn(columns);

            columnService.deleteAllByBoardId(boardId);

            verify(taskAssigneeRepository).deleteByColumnIn(columns);
            verify(taskRepository).deleteByColumnIn(columns);
            verify(columnRepository).deleteAllByBoardId(boardId);

            InOrder inOrder = inOrder(columnRepository, taskAssigneeRepository, taskRepository);
            inOrder.verify(columnRepository).findAllByBoardId(boardId);
            inOrder.verify(taskAssigneeRepository).deleteByColumnIn(columns);
            inOrder.verify(taskRepository).deleteByColumnIn(columns);
            inOrder.verify(columnRepository).deleteAllByBoardId(boardId);

            verifyNoMoreInteractions(columnRepository, taskAssigneeRepository, taskRepository);
        }

        @Test
        void shouldStillCallDeleteAllByBoardId_WhenNoColumnsExist() {
            Integer boardId = 99;
            when(columnRepository.findAllByBoardId(boardId)).thenReturn(List.of());

            columnService.deleteAllByBoardId(boardId);

            verify(taskAssigneeRepository).deleteByColumnIn(List.of());
            verify(taskRepository).deleteByColumnIn(List.of());
            verify(columnRepository).deleteAllByBoardId(boardId);
        }
    }
}
