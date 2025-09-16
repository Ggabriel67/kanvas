package io.github.ggabriel67.kanvas.task.assignee;

import io.github.ggabriel67.kanvas.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Integer>
{
    void deleteAllByTaskId(Integer taskId);

    @Transactional
    @Modifying
    @Query("""
DELETE FROM TaskAssignee ta
WHERE ta.task.column.id = :columnId
""")
    void deleteAllByColumnId(@Param("columnId") Integer columnId);

    Optional<TaskAssignee> findByBoardMemberIdAndTaskId(Integer boardMemberId, Integer taskId);
}
