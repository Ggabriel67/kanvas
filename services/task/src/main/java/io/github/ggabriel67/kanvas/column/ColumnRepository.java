package io.github.ggabriel67.kanvas.column;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<Column, Integer>
{
    @Query("""
SELECT MAX(c.orderIndex) FROM Column c WHERE c.boardId = :boardId
""")
    Double findMaxOrderIndexByBoardId(@Param("boardId") Integer boardId);

    @Query("""
SELECT new io.github.ggabriel67.kanvas.column.ColumnTaskFlatDto(
    c.id, c.orderIndex, c.name, t.id, t.orderIndex, t.title, t.deadline, t.status, t.priority, ta.boardMemberId
)
FROM Column c
LEFT JOIN Task t ON t.column = c
LEFT JOIN TaskAssignee ta ON ta.task = t
WHERE c.boardId = :boardId
""")
    List<ColumnTaskFlatDto> findColumnDataByBoardId(@Param("boardId") Integer boardId);

    
    void deleteAllByBoardId(Integer boardId);

    List<Column> findAllByBoardId(Integer boardId);
}
