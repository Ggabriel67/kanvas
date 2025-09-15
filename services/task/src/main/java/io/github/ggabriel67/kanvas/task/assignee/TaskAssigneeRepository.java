package io.github.ggabriel67.kanvas.task.assignee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, Integer>
{

}
