package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.column.ColumnService;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.workspace.WorkspaceDeleted;
import io.github.ggabriel67.kanvas.event.workspace.WorkspaceEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceEventConsumer
{
    private final ObjectMapper objectMapper;
    private final ColumnService columnService;

    @KafkaListener(topics = "workspace.events")
    public void consumeWorkspaceEvent(Event<?> event) {
        WorkspaceEventType eventType = WorkspaceEventType.valueOf(event.getEventType());
        switch (eventType) {
            case WORKSPACE_DELETED -> {
                WorkspaceDeleted workspaceDeleted = objectMapper.convertValue(event.getPayload(), WorkspaceDeleted.class);
                handleWorkspaceDeleted(workspaceDeleted);
            }
            default -> {}
        }
    }

    private void handleWorkspaceDeleted(WorkspaceDeleted workspaceDeleted) {
        columnService.deleteAllByBoardIds(workspaceDeleted.boardIds());
    }
}
