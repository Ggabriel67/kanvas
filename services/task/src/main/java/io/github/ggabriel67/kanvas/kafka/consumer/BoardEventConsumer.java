package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.column.ColumnService;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.board.BoardDeleted;
import io.github.ggabriel67.kanvas.event.board.BoardEventType;
import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.task.assignee.TaskAssigneeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardEventConsumer
{
    private final ObjectMapper objectMapper;
    private final ColumnService columnService;
    private final TaskAssigneeService taskAssigneeService;

    @KafkaListener(topics = "board.events")
    public void consumeBoardEvent (Event<?> event) {
        log.info("Consuming message from 'board.events' topic");
        BoardEventType eventType = BoardEventType.valueOf(event.getEventType());
        switch (eventType) {
            case DELETED -> {
                BoardDeleted boardDeleted = objectMapper.convertValue(event.getPayload(), BoardDeleted.class);
                handleBoardDeleted(boardDeleted);
            }
            case MEMBER_REMOVED -> {
                BoardMemberRemoved memberRemoved = objectMapper.convertValue(event.getPayload(), BoardMemberRemoved.class);
                handleMemberRemoved(memberRemoved);
            }
            default -> {}
        }
    }

    private void handleBoardDeleted(BoardDeleted boardDeleted) {
        log.info("Handling board deleted");
        columnService.deleteAllByBoardId(boardDeleted.boardId());
    }

    private void handleMemberRemoved(BoardMemberRemoved memberRemoved) {
        log.info("Handling member removed");
        taskAssigneeService.deleteMemberAssignments(memberRemoved);
    }
}
