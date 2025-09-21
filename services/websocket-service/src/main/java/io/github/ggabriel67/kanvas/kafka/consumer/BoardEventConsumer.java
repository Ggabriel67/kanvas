package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.board.BoardEventType;
import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.event.board.BoardUpdated;
import io.github.ggabriel67.kanvas.message.MessageService;
import io.github.ggabriel67.kanvas.message.board.BoardMessage;
import io.github.ggabriel67.kanvas.message.board.BoardMessageType;
import io.github.ggabriel67.kanvas.message.board.board.BoardUpdatedMessage;
import io.github.ggabriel67.kanvas.message.board.board.MemberRemovedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardEventConsumer
{
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @KafkaListener(topics = "board.events")
    public void consumeBoardEvent(Event<?> event) {
        log.info("Consuming message from 'board.events' topic");
        BoardEventType eventType = BoardEventType.valueOf(event.getEventType());
        switch (eventType) {
            case MEMBER_REMOVED -> {
                BoardMemberRemoved memberRemoved = objectMapper.convertValue(event.getPayload(), BoardMemberRemoved.class);
                handleBoardMemberRemoved(memberRemoved);
            }
            case BOARD_UPDATED -> {
                BoardUpdated boardUpdated = objectMapper.convertValue(event.getPayload(), BoardUpdated.class);
                handleBoardUpdated(boardUpdated);
            }
        }
    }

    private void handleBoardMemberRemoved(BoardMemberRemoved memberRemoved) {
        BoardMessage<MemberRemovedMessage> message = new BoardMessage<>(
                BoardMessageType.MEMBER_REMOVED,
                new MemberRemovedMessage(memberRemoved.memberId())
        );
        messageService.sendBoardMessage(memberRemoved.boardId(), message);
    }

    private void handleBoardUpdated(BoardUpdated boardUpdated) {
        BoardMessage<BoardUpdatedMessage> message = new BoardMessage<>(
                BoardMessageType.BOARD_UPDATED,
                new BoardUpdatedMessage(boardUpdated.name(), boardUpdated.description(), boardUpdated.visibility())
        );
        messageService.sendBoardMessage(boardUpdated.boardId(), message);
    }
}
