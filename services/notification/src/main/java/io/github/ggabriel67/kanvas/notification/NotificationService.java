package io.github.ggabriel67.kanvas.notification;

import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.event.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.event.invitation.InvitationUpdate;
import io.github.ggabriel67.kanvas.event.task.TaskAssignment;
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

    public void createAssignmentNotification(TaskAssignment assignment) {
        Notification notification = repository.save(
                Notification.builder()
                        .userId(assignment.userId())
                        .type(NotificationType.ASSIGNMENT)
                        .status(NotificationStatus.UNREAD)
                        .payload(Map.of(
                                "taskTitle", assignment.taskTitle(),
                                "boardName", assignment.boardName(),
                                "assigned", assignment.assigned()
                                )
                        )
                        .build()
        );
    }

    public void createBoardMemberRemovedNotification(BoardMemberRemoved memberRemoved) {
        Notification notification = repository.save(
                Notification.builder()
                        .userId(memberRemoved.userId())
                        .type(NotificationType.REMOVED_FROM_BOARD)
                        .status(NotificationStatus.UNREAD)
                        .payload(Map.of(
                                "boardName", memberRemoved.boardName()
                                )
                        )
                        .build()
        );
    }
}
