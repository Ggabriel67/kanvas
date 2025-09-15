package io.github.ggabriel67.kanvas.column;

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
@Table(name = "columns")
public class Column
{
    @Id
    @GeneratedValue
    private Integer id;
    private double orderIndex;
    private String name;

    private Integer boardId;
}
