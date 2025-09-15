package io.github.ggabriel67.kanvas.column;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnRepository extends JpaRepository<Column, Integer>
{
    @Query("""
SELECT MAX(c.orderIndex) FROM Column c WHERE c.boardId = :boardId
""")
    Double findMaxOrderIndexByBoardId(@Param("boardId") Integer boardId);
}
