package io.github.ggabriel67.kanvas.notification;

import io.github.ggabriel67.kanvas.event.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.event.invitation.InvitationUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService
{
    private final NotificationRepository repository;

    public void createInvitationNotification(InvitationCreated invitation) {
        Notification notification = repository.save(
                Notification.builder()
                        .userId(invitation.inviteeId())
                        .type(NotificationType.INVITATION)
                        .status(NotificationStatus.UNREAD)
                        .payload(Map.of(
                                "invitationId", invitation.invitationId(),
                                "inviterUsername", invitation.inviterUsername(),
                                "targetName", invitation.targetName(),
                                "scope", invitation.scope()
                                )
                        )
                        .build()
        );
    }

    public void updateInvitationNotification(InvitationUpdate invUpdate) {
        Notification notification = repository.findByInvitationIdAndUserId(invUpdate.invitationId(), invUpdate.inviteeId());
        notification.setStatus(NotificationStatus.DELETED);
        repository.save(notification);
    }
}
