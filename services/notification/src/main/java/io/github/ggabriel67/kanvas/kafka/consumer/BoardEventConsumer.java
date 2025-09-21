package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.board.BoardEventType;
import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.notification.NotificationService;
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
    private final NotificationService notificationService;

    @KafkaListener(topics = "board.events")
    public void consumeBoardEvent(Event<?> event) {
        log.info("Consuming message from 'board.events' topic");
        BoardEventType eventType = BoardEventType.valueOf(event.getEventType());
        if (eventType == BoardEventType.MEMBER_REMOVED) {
            BoardMemberRemoved boardMemberRemoved = objectMapper.convertValue(event.getPayload(), BoardMemberRemoved.class);
            handleMemberRemoved(boardMemberRemoved);
        }
    }

    private void handleMemberRemoved(BoardMemberRemoved boardMemberRemoved) {
        notificationService.createBoardMemberRemovedNotification(boardMemberRemoved);
    }
}
