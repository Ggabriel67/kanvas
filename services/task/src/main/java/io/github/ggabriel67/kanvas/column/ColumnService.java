package io.github.ggabriel67.kanvas.column;

import io.github.ggabriel67.kanvas.exception.ColumnNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ColumnService
{
    private final ColumnRepository columnRepository;
    private final ColumnMapper columnMapper;

    @Value("${application.column.step}")
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
                        .name(request.columnName())
                        .orderIndex(maxOrderIndex + step)
                        .build()
        );

        return columnMapper.toColumnResponse(column);
    }

    @Transactional
    public ColumnResponse moveColumn(MoveColumnRequest request) {
        double newOrderIndex;
        Column preceding;
        Column following;
        if (request.precedingColumnId() == null) {
            following = getColumnById(request.followingColumnId());

            newOrderIndex = following.getOrderIndex() - step;
        }
        else if (request.followingColumnId() == null) {
            preceding = getColumnById(request.precedingColumnId());

            newOrderIndex = preceding.getOrderIndex() - step;
        }
        else {
            preceding = getColumnById(request.precedingColumnId());
            following = getColumnById(request.followingColumnId());

            newOrderIndex = (preceding.getOrderIndex() + following.getOrderIndex()) / 2;
        }

        Column column = getColumnById(request.columnId());

        column.setOrderIndex(newOrderIndex);
        columnRepository.save(column);
        return columnMapper.toColumnResponse(column);
    }

    public void updateColumnName(Integer columnId, ColumnRequest request) {
        Column column = getColumnById(columnId);
        column.setName(request.columnName());
        columnRepository.save(column);
    }
}
