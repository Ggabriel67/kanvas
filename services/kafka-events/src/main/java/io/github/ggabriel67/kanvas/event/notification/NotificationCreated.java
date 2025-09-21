package io.github.ggabriel67.kanvas.event.notification;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationCreated(
        Integer notificationId,
        Integer userId,
        String type,
        String status,
        LocalDateTime sentAt,
        Map<String, Object> payload
) {
}
