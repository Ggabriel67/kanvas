package io.github.ggabriel67.kanvas.notification;

import java.util.Map;

public record NotificationDto(
        Integer notificationId,
        Integer userId,
        NotificationType type,
        NotificationStatus status,
        java.time.LocalDateTime sentAt,
        Map<String, Object> payload
) {
}
