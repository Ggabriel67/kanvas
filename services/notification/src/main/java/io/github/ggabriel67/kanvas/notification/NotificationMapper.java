package io.github.ggabriel67.kanvas.notification;

import org.springframework.stereotype.Service;

@Service
public class NotificationMapper
{
    public NotificationDto toNotificationDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getPayload()
        );
    }
}
