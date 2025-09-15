package io.github.ggabriel67.kanvas.task.assignee;

import io.github.ggabriel67.kanvas.task.Task;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "task_assignees")
public class TaskAssignee
{
    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    private Integer boardMemberId;
}
