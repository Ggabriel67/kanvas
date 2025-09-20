package io.github.ggabriel67.kanvas.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController
{
    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotifications(@PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @PatchMapping("/{notificationId}/status/{status}")
    public ResponseEntity<Void> updateNotificationStatus(@PathVariable("notificationId") Integer notificationId,
                                                         @PathVariable("status") NotificationStatus status) {
        notificationService.updateNotificationStatus(notificationId, status);
        return ResponseEntity.ok().build();
    }

}
