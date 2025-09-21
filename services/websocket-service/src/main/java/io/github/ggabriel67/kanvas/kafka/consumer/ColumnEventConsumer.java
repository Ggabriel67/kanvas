package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.column.*;
import io.github.ggabriel67.kanvas.message.MessageService;
import io.github.ggabriel67.kanvas.message.board.BoardMessage;
import io.github.ggabriel67.kanvas.message.board.BoardMessageType;
import io.github.ggabriel67.kanvas.message.board.column.ColumnCreatedMessage;
import io.github.ggabriel67.kanvas.message.board.column.ColumnDeletedMessage;
import io.github.ggabriel67.kanvas.message.board.column.ColumnMovedMessage;
import io.github.ggabriel67.kanvas.message.board.column.ColumnUpdatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnEventConsumer
{
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @KafkaListener(topics = "column.events")
    public void consumeColumnEvent(Event<?> event) {
        log.info("Consuming message from 'column.events' topic");
        ColumnEventType eventType = ColumnEventType.valueOf(event.getEventType());
        switch (eventType) {
            case CREATED -> {
                ColumnCreated columnCreated = objectMapper.convertValue(event.getPayload(), ColumnCreated.class);
                handleColumnCreated(columnCreated);
            }
            case UPDATED -> {
                ColumnUpdated columnUpdated = objectMapper.convertValue(event.getPayload(), ColumnUpdated.class);
                handleColumnUpdated(columnUpdated);
            }
            case MOVED -> {
                ColumnMoved columnMoved = objectMapper.convertValue(event.getPayload(), ColumnMoved.class);
                handleColumnMoved(columnMoved);
            }
            case DELETED -> {
                ColumnDeleted columnDeleted = objectMapper.convertValue(event.getPayload(), ColumnDeleted.class);
                handleColumnDeleted(columnDeleted);
            }
        }
    }

    private void handleColumnCreated(ColumnCreated colCreated) {
        BoardMessage<ColumnCreatedMessage> message = new BoardMessage<>(
                BoardMessageType.COLUMN_CREATED,
                new ColumnCreatedMessage(colCreated.columnId(), colCreated.orderIndex(), colCreated.name())
        );
        messageService.sendBoardMessage(colCreated.boardId(), message);
    }

    private void handleColumnUpdated(ColumnUpdated colUpdated) {
        BoardMessage<ColumnUpdatedMessage> message = new BoardMessage<>(
                BoardMessageType.COLUMN_UPDATED,
                new ColumnUpdatedMessage(colUpdated.columnId(), colUpdated.columnName())
        );
        messageService.sendBoardMessage(colUpdated.boardId(), message);
    }

    private void handleColumnMoved(ColumnMoved colMoved) {
        BoardMessage<ColumnMovedMessage> message = new BoardMessage<>(
                BoardMessageType.COLUMN_MOVED,
                new ColumnMovedMessage(colMoved.columnId(), colMoved.newOrderIndex())
        );
        messageService.sendBoardMessage(colMoved.boardId(), message);
    }

    private void handleColumnDeleted(ColumnDeleted colDeleted) {
        BoardMessage<ColumnDeletedMessage> message = new BoardMessage<>(
                BoardMessageType.COLUMN_DELETED,
                new ColumnDeletedMessage(colDeleted.columnId())
        );
        messageService.sendBoardMessage(colDeleted.boardId(), message);
    }
}
