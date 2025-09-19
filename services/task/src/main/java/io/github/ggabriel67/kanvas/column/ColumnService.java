package io.github.ggabriel67.kanvas.column;

import io.github.ggabriel67.kanvas.event.column.ColumnCreated;
import io.github.ggabriel67.kanvas.event.column.ColumnDeleted;
import io.github.ggabriel67.kanvas.event.column.ColumnMoved;
import io.github.ggabriel67.kanvas.event.column.ColumnUpdated;
import io.github.ggabriel67.kanvas.exception.ColumnNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.ColumnEventProducer;
import io.github.ggabriel67.kanvas.task.TaskDtoProjection;
import io.github.ggabriel67.kanvas.task.TaskRepository;
import io.github.ggabriel67.kanvas.task.TaskService;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColumnService
{
    private final ColumnRepository columnRepository;
    private final ColumnMapper columnMapper;
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final ColumnEventProducer columnEventProducer;

    @Value("${application.ordering.step.column}")
    private Double step;

    private Column getColumnById(Integer id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new ColumnNotFoundException("Column not found"));
    }

    public ColumnResponse createColumn(ColumnRequest request) {
        Double maxOrderIndex = columnRepository.findMaxOrderIndexByBoardId(request.boardId());
        if (maxOrderIndex == null) {
            maxOrderIndex = 0d;
        }

        Column column = columnRepository.save(
                Column.builder()
                        .name(request.name())
                        .orderIndex(maxOrderIndex + step)
                        .boardId(request.boardId())
                        .build()
        );

        columnEventProducer.sendColumnCreated(new ColumnCreated(
                column.getId(), column.getOrderIndex(), column.getName(), column.getBoardId())
        );
        return columnMapper.toColumnResponse(column);
    }

    public void updateColumnName(Integer columnId, ColumnRequest request) {
        Column column = getColumnById(columnId);
        column.setName(request.name());
        columnRepository.save(column);

        columnEventProducer.sendColumnUpdated(new ColumnUpdated(
               column.getBoardId(), column.getId() , column.getName())
        );
    }

    @Transactional
    public ColumnResponse moveColumn(MoveColumnRequest request) {
        double newOrderIndex;
        Column preceding, following;
        if (request.precedingColumnId() == null) {
            following = getColumnById(request.followingColumnId());
            newOrderIndex = following.getOrderIndex() - step;
        }
        else if (request.followingColumnId() == null) {
            preceding = getColumnById(request.precedingColumnId());
            newOrderIndex = preceding.getOrderIndex() + step;
        }
        else {
            preceding = getColumnById(request.precedingColumnId());
            following = getColumnById(request.followingColumnId());
            newOrderIndex = (preceding.getOrderIndex() + following.getOrderIndex()) / 2;
        }

        Column column = getColumnById(request.columnId());

        column.setOrderIndex(newOrderIndex);
        columnRepository.save(column);

        columnEventProducer.sendColumnMoved(new ColumnMoved(
                column.getBoardId(), column.getId(), newOrderIndex)
        );

        return columnMapper.toColumnResponse(column);
    }

    public List<ColumnDto> getAllBoardColumns(Integer boardId) {
        List<ColumnTaskFlatDto> flatResults = columnRepository.findColumnDataByBoardId(boardId);
        Map<Integer, List<ColumnTaskFlatDto>> groupedByTask = flatResults.stream()
                .filter(r -> r.taskId() != null)
                .collect(Collectors.groupingBy(ColumnTaskFlatDto::taskId));

        List<TaskDtoProjection> taskProjections = groupedByTask.values().stream()
                .map(flatResultValue -> {
                    var flatTask = flatResultValue.getFirst();
                    var assigneeIds = flatResultValue.stream()
                            .map(ColumnTaskFlatDto::assigneeId)
                            .filter(Objects::nonNull)
                            .toList();
                    return new TaskDtoProjection(flatTask.taskId(), flatTask.taskOrderIndex(), flatTask.columnId(), flatTask.taskTitle(), flatTask.deadline(),
                            flatTask.status(), flatTask.priority(), assigneeIds, taskService.isTaskExpired(flatTask.status(), flatTask.deadline())
                    );
                })
                .toList();

        Map<Integer, ColumnDto> groupedColumns = new HashMap<>();
        for (ColumnTaskFlatDto flat : flatResults) {
            groupedColumns.computeIfAbsent(flat.columnId(),
                    columnId -> new ColumnDto(columnId, flat.columnOrderIndex(), flat.columnName(), new ArrayList<>())
            );
        }

        for (TaskDtoProjection task : taskProjections) {
            groupedColumns.get(task.columnId()).taskProjections().add(task);
        }

        List<ColumnDto> columns = groupedColumns.values().stream().toList();
        return columns.stream()
                .map(column -> new ColumnDto(column.columnId(), column.orderIndex(), column.name(),
                        column.taskProjections().stream()
                                .sorted(Comparator.comparingDouble(TaskDtoProjection::orderIndex))
                                .toList()
                ))
                .sorted(Comparator.comparingDouble(ColumnDto::orderIndex))
                .toList();
    }

    @Transactional
    public void deleteColumn(Integer columnId) {
        Column column = getColumnById(columnId);
        ColumnDeleted columnDeleted = new ColumnDeleted(columnId, column.getBoardId());

        taskAssigneeRepository.deleteAllByColumnId(columnId);
        taskRepository.deleteAllByColumnId(columnId);
        columnRepository.delete(column);

        columnEventProducer.sendColumnDeleted(columnDeleted);
    }

    @Transactional
    public void deleteAllByBoardId(Integer boardId) {
        List<Column> columns = columnRepository.findAllByBoardId(boardId);
        taskAssigneeRepository.deleteByColumnIn(columns);
        taskRepository.deleteByColumnIn(columns);
        columnRepository.deleteAllByBoardId(boardId);
    }
}
