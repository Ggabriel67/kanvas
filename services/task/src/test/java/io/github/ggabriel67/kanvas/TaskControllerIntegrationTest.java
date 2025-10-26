package io.github.ggabriel67.kanvas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.column.Column;
import io.github.ggabriel67.kanvas.column.ColumnRepository;
import io.github.ggabriel67.kanvas.column.ColumnResponse;
import io.github.ggabriel67.kanvas.kafka.producer.ColumnEventProducer;
import io.github.ggabriel67.kanvas.kafka.producer.TaskEventProducer;
import io.github.ggabriel67.kanvas.task.*;
import io.github.ggabriel67.kanvas.task.assignee.AssignmentRequest;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerIntegrationTest
{
    @MockitoBean
    private ColumnEventProducer columnEventProducer;

    @MockitoBean
    private TaskEventProducer taskEventProducer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ColumnRepository columnRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskAssigneeRepository taskAssigneeRepository;

    @BeforeEach
    void cleanDatabase() {
        taskAssigneeRepository.deleteAll();
        taskRepository.deleteAll();
        columnRepository.deleteAll();
    }

    @Value("${application.ordering.step.task}")
    private Double step;

    @Test
    void createTask_ShouldCreateTaskSuccessfully() throws Exception {
        Column column = Column.builder().name("Old name").orderIndex(step).build();
        columnRepository.save(column);

        TaskRequest request = new TaskRequest(column.getId(), "Test task");

        var result = mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Board-Role", "EDITOR"))
                .andExpect(status().isCreated())
                .andReturn();

        var taskResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TaskResponse.class);

        var savedTask = taskRepository.findById(taskResponse.taskId());
        assertThat(savedTask.isPresent());
        assertThat(savedTask.get().getOrderIndex()).isEqualTo(step);
        assertThat(savedTask.get().getTitle()).isEqualTo("Test task");
    }

    @Test
    void moveTask_ShouldMoveTaskToOtherColumn() throws Exception {
        Column column1 = Column.builder().name("Column 1").build();
        Column column2 = Column.builder().name("Column 2").build();
        columnRepository.saveAll(List.of(column1, column2));

        Task task1 = Task.builder().column(column1).title("Moved Task").orderIndex(step).build();
        Task task2 = Task.builder().column(column2).title("Column 2 Task").orderIndex(step).build();
        taskRepository.saveAll(List.of(task1, task2));

        MoveTaskRequest request = new MoveTaskRequest(
            column2.getId(), task1.getId(), null, task2.getId()
        );

        var result = mockMvc.perform(patch("/api/v1/tasks/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Board-Role", "EDITOR"))
                .andExpect(status().isOk())
                .andReturn();

        var taskResponse = objectMapper.readValue(result.getResponse().getContentAsString(), TaskResponse.class);

        var movedTask = taskRepository.findById(taskResponse.taskId()).get();
        assertThat(movedTask.getColumn().getId()).isEqualTo(column2.getId());
        assertThat(movedTask.getOrderIndex()).isEqualTo(task2.getOrderIndex() - step);
    }

    @Test
    void assignTask_ShouldAssignTaskToUserSuccessfully() throws Exception {
        Column column = Column.builder().name("Column 1").build();
        columnRepository.save(column);

        Task task = Task.builder().column(column).title("Moved Task").build();
        taskRepository.save(task);

        Integer boardMemberId = 10;
        AssignmentRequest request = new AssignmentRequest(
                task.getId(), boardMemberId, 20, 10, "Test Board"
        );

        mockMvc.perform(post("/api/v1/tasks/assignees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Board-Role", "EDITOR"))
                .andExpect(status().isCreated())
                .andReturn();

        var savedAssignee = taskAssigneeRepository.findByBoardMemberIdAndTaskId(boardMemberId, task.getId());
        assertThat(savedAssignee.isPresent());
    }
}
