package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.board.*;
import io.github.ggabriel67.kanvas.message.MessageService;
import io.github.ggabriel67.kanvas.message.board.BoardMessage;
import io.github.ggabriel67.kanvas.message.board.BoardMessageType;
import io.github.ggabriel67.kanvas.message.board.board.BoardUpdatedMessage;
import io.github.ggabriel67.kanvas.message.board.board.MemberJoinedMessage;
import io.github.ggabriel67.kanvas.message.board.board.MemberRemovedMessage;
import io.github.ggabriel67.kanvas.message.board.board.RoleChangedMessage;
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
            case MEMBER_REMOVED, MEMBER_LEFT -> {
                BoardMemberRemoved memberRemoved = objectMapper.convertValue(event.getPayload(), BoardMemberRemoved.class);
                handleBoardMemberRemoved(memberRemoved);
            }
            case BOARD_UPDATED -> {
                BoardUpdated boardUpdated = objectMapper.convertValue(event.getPayload(), BoardUpdated.class);
                handleBoardUpdated(boardUpdated);
            }
            case MEMBER_JOINED -> {
                BoardMemberJoined memberJoined = objectMapper.convertValue(event.getPayload(), BoardMemberJoined.class);
                handleMemberJoined(memberJoined);
            }
            case ROLE_CHANGED -> {
                RoleChanged roleChanged = objectMapper.convertValue(event.getPayload(), RoleChanged.class);
                handleRoleChanged(roleChanged);
            }
        }
    }

    private void handleMemberJoined(BoardMemberJoined memberJoined) {
        BoardMessage<MemberJoinedMessage> message = new BoardMessage<>(
                BoardMessageType.MEMBER_JOINED,
                new MemberJoinedMessage(memberJoined.memberId(), memberJoined.userId(), memberJoined.firstname(), memberJoined.lastname(),
                        memberJoined.username(), memberJoined.avatarColor(), memberJoined.boardRole(), memberJoined.joinedAt()
                )
        );
        messageService.sendBoardMessage(memberJoined.boardId(), message);
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

    private void handleRoleChanged(RoleChanged roleChanged) {
        BoardMessage<RoleChangedMessage> message = new BoardMessage<>(
                BoardMessageType.ROLE_CHANGED,
                new RoleChangedMessage(roleChanged.memberId(), roleChanged.role())
        );
        messageService.sendBoardMessage(roleChanged.boardId(), message);
    }
}
