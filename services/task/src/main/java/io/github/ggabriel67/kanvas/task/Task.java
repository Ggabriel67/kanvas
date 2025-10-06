package io.github.ggabriel67.kanvas.task;

import io.github.ggabriel67.kanvas.column.Column;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tasks")
public class Task
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "column_id")
    private Column column;

    private double orderIndex;
    private String title;
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String description;
    private Instant deadline;

    @CreatedDate
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    public boolean isExpired() {
        return status != TaskStatus.DONE && deadline != null &&
                deadline.isBefore(Instant.now());
    }
}
