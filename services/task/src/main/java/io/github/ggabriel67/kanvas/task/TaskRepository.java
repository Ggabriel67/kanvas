package io.github.ggabriel67.kanvas.task;

import io.github.ggabriel67.kanvas.column.Column;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer>
{
    @Query("""
SELECT MAX(t.orderIndex) FROM Task t WHERE t.column = :column
""")
    Double findMaxOrderIndexByColumn(@Param("column") Column column);
}
