package io.github.ggabriel67.kanvas.column;

import org.springframework.stereotype.Service;

@Service
public class ColumnMapper
{
    public ColumnResponse toColumnResponse(Column column) {
        return new ColumnResponse(column.getId(), column.getName(), column.getOrderIndex());
    }
}
