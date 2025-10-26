package io.github.ggabriel67.kanvas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.column.*;
import io.github.ggabriel67.kanvas.kafka.producer.ColumnEventProducer;
import io.github.ggabriel67.kanvas.kafka.producer.TaskEventProducer;
import jakarta.validation.Valid;
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
public class ColumnControllerIntegrationTest
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

    @BeforeEach
    void cleanDatabase() {
        columnRepository.deleteAll();
    }

    @Value("${application.ordering.step.column}")
    private Double step;

    @Test
    void createColumn_ShouldCreateColumnSuccessfully() throws Exception {
        ColumnRequest request = new ColumnRequest(10, "Test Column");

        var result = mockMvc.perform(post("/api/v1/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Board-Role", "EDITOR"))
                .andExpect(status().isCreated())
                .andReturn();

        var columnResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ColumnResponse.class);

        var savedColumn = columnRepository.findById(columnResponse.columnId());
        assertThat(savedColumn.isPresent());
        assertThat(savedColumn.get().getOrderIndex()).isEqualTo(step);
        assertThat(savedColumn.get().getName()).isEqualTo("Test Column");
    }

    @Test
    void moveColumn_ShouldMoveBetweenTwoColumnsSuccessfully() throws Exception {
        Column c1 = Column.builder().name("Existing Column 1").orderIndex(step).build();
        Column c2 = Column.builder().name("Existing Column 2").orderIndex(step * 2).build();
        Column c3 = Column.builder().name("Existing Column 3").orderIndex(step * 3).build();
        columnRepository.saveAll(List.of(c1, c2, c3));

        MoveColumnRequest request = new MoveColumnRequest(c1.getId(), c2.getId(), c3.getId());

        var result = mockMvc.perform(patch("/api/v1/columns/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Board-Role", "EDITOR"))
                .andExpect(status().isOk())
                .andReturn();

        var columnResponse = objectMapper.readValue(result.getResponse().getContentAsString(), ColumnResponse.class);

        var movedColumn = columnRepository.findById(columnResponse.columnId()).get();
        assertThat(movedColumn.getOrderIndex()).isEqualTo((c2.getOrderIndex() + c3.getOrderIndex()) / 2);
    }

    @Test
    void updateColumnName_ShouldUpdateColumnNameSuccessfully() throws Exception {
        Column column = Column.builder().name("Old name").orderIndex(step).build();
        columnRepository.save(column);

        ColumnRequest request = new ColumnRequest(column.getId(), "New Column");

        mockMvc.perform(patch("/api/v1/columns/{columnId}", column.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-Board-Role", "EDITOR"))
                .andExpect(status().isOk())
                .andReturn();

        var updatedColumn = columnRepository.findById(column.getId()).get();
        assertThat(updatedColumn.getName()).isEqualTo("New Column");
    }
}
