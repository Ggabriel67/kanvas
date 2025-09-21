package io.github.ggabriel67.kanvas.message.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage
{
    Integer notificationId;
    Integer userId;
    String type;
    String status;
    LocalDateTime sentAt;
    Map<String, Object> payload;
}
