package io.github.ggabriel67.kanvas.message;

import io.github.ggabriel67.kanvas.event.notification.NotificationCreated;
import io.github.ggabriel67.kanvas.message.board.BoardMessage;
import io.github.ggabriel67.kanvas.message.notification.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService
{
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(NotificationCreated notificationCreated) {
        NotificationMessage message = new NotificationMessage(
                notificationCreated.notificationId(),
                notificationCreated.userId(),
                notificationCreated.type(),
                notificationCreated.status(),
                notificationCreated.sentAt(),
                notificationCreated.payload()
        );
        messagingTemplate.convertAndSendToUser(
                notificationCreated.userId().toString(),
                "/notifications",
                message);
    }

    public void sendBoardMessage(Integer boardId, BoardMessage<?> message) {
        messagingTemplate.convertAndSend(
                "/topic/board." + boardId,
                message
        );
    }
}
