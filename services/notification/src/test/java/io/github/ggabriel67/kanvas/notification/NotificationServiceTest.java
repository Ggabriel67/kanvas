package io.github.ggabriel67.kanvas.notification;

import io.github.ggabriel67.kanvas.event.board.BoardMemberRemoved;
import io.github.ggabriel67.kanvas.event.invitation.InvitationCreated;
import io.github.ggabriel67.kanvas.event.invitation.InvitationUpdate;
import io.github.ggabriel67.kanvas.event.task.TaskAssignment;
import io.github.ggabriel67.kanvas.exception.NotificationNotFoundException;
import io.github.ggabriel67.kanvas.kafka.producer.NotificationEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest
{
    @Mock private NotificationRepository repository;
    @Mock private NotificationMapper notificationMapper;
    @Mock private NotificationEventProducer eventProducer;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldCreateAndSendInvitationNotification() {
        InvitationCreated invitation = new InvitationCreated(
                10, 5, "alice", "Workspace Alpha", "WORKSPACE"
        );

        Notification savedNotification = Notification.builder()
                .id(100)
                .userId(5)
                .type(NotificationType.INVITATION)
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.UNREAD)
                .payload(Map.of(
                        "invitationId", 10,
                        "inviterUsername", "alice",
                        "targetName", "Workspace Alpha",
                        "scope", "WORKSPACE"
                ))
                .build();

        when(repository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.createInvitationNotification(invitation);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        Notification toSave = captor.getValue();

        assertThat(toSave.getUserId()).isEqualTo(5);
        assertThat(toSave.getType()).isEqualTo(NotificationType.INVITATION);
        assertThat(toSave.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(toSave.getPayload()).isEqualTo(Map.of(
                "invitationId", 10,
                "inviterUsername", "alice",
                "targetName", "Workspace Alpha",
                "scope", "WORKSPACE"
        ));

        verify(eventProducer).sendNotificationCreated(argThat(event ->
                event.notificationId().equals(savedNotification.getId())
                        && event.userId().equals(savedNotification.getUserId())
                        && event.type().equals(savedNotification.getType().name())
                        && event.status().equals(savedNotification.getStatus().name())
                        && event.sentAt().equals(savedNotification.getSentAt())
                        && event.payload().equals(savedNotification.getPayload())
        ));
    }

    @Nested
    class CreateAssignmentNotification {
        @Test
        void shouldCreateAssignmentNotification_AndSendKafkaEvent() {
            TaskAssignment assignment = new TaskAssignment(10, 50, 99, 5, 6,
                    "Fix Login Bug", "Engineering Board", true);

            Notification savedNotification = Notification.builder()
                    .id(100)
                    .userId(assignment.userId())
                    .type(NotificationType.ASSIGNMENT)
                    .status(NotificationStatus.UNREAD)
                    .sentAt(LocalDateTime.now())
                    .payload(Map.of(
                            "taskTitle", assignment.taskTitle(),
                            "boardName", assignment.boardName(),
                            "assigned", assignment.assigned()
                    ))
                    .build();

            when(repository.save(any(Notification.class))).thenReturn(savedNotification);

            notificationService.createAssignmentNotification(assignment);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(repository).save(captor.capture());
            Notification saved = captor.getValue();

            assertThat(saved.getUserId()).isEqualTo(assignment.userId());
            assertThat(saved.getType()).isEqualTo(NotificationType.ASSIGNMENT);
            assertThat(saved.getStatus()).isEqualTo(NotificationStatus.UNREAD);
            assertThat(saved.getPayload().get("taskTitle")).isEqualTo(assignment.taskTitle());
            assertThat(saved.getPayload().get("boardName")).isEqualTo(assignment.boardName());
            assertThat(saved.getPayload().get("assigned")).isEqualTo(assignment.assigned());

            verify(eventProducer).sendNotificationCreated(argThat(event ->
                    event.notificationId().equals(savedNotification.getId()) &&
                            event.userId().equals(savedNotification.getUserId()) &&
                            event.type().equals(savedNotification.getType().name()) &&
                            event.status().equals(savedNotification.getStatus().name()) &&
                            event.sentAt().equals(savedNotification.getSentAt()) &&
                            event.payload().equals(savedNotification.getPayload())
            ));
        }

        @Test
        void shouldNotCreateNotification_WhenUserAssignsTaskToSelf() {
            TaskAssignment selfAssignment = new TaskAssignment(10, 50, 99, 5, 5, "Fix Login Bug", "Engineering Board", true);
            notificationService.createAssignmentNotification(selfAssignment);

            verifyNoInteractions(repository);
            verifyNoInteractions(eventProducer);
        }
    }

    @Test
    void shouldCreateNotification_WhenBoardMemberRemoved() {
        BoardMemberRemoved memberRemoved = new BoardMemberRemoved(101, 5, 42, "Board");

        Notification savedNotification = Notification.builder()
                .id(100)
                .userId(5)
                .type(NotificationType.REMOVED_FROM_BOARD)
                .status(NotificationStatus.UNREAD)
                .payload(Map.of("boardName", "Board"))
                .build();

        when(repository.save(any(Notification.class))).thenReturn(savedNotification);

        notificationService.createBoardMemberRemovedNotification(memberRemoved);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();

        assertThat(captured.getUserId()).isEqualTo(5);
        assertThat(captured.getType()).isEqualTo(NotificationType.REMOVED_FROM_BOARD);
        assertThat(captured.getStatus()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(captured.getPayload().get("boardName")).isEqualTo("Board");

        verify(eventProducer).sendNotificationCreated(argThat(event ->
                event.notificationId().equals(savedNotification.getId())
                        && event.userId().equals(savedNotification.getUserId())
                        && event.type().equals(savedNotification.getType().name())
                        && event.status().equals(savedNotification.getStatus().name())
                        && event.payload().equals(savedNotification.getPayload())
        ));
    }

    @Nested
    class GetNotificationsTests {
        @Test
        void shouldReturnMappedNotifications_WhenUserHasNotifications() {
            Integer userId = 5;

            Notification notification1 = Notification.builder().id(100).userId(userId).type(NotificationType.INVITATION)
                    .status(NotificationStatus.UNREAD).payload(Map.of("inviter", "Alice")).build();

            Notification notification2 = Notification.builder().id(101).userId(userId).type(NotificationType.ASSIGNMENT).status(NotificationStatus.UNREAD)
                    .payload(Map.of("taskTitle", "Implement feature X")).build();

            NotificationDto dto1 = new NotificationDto(
                    100, userId, NotificationType.INVITATION, NotificationStatus.UNREAD, null, Map.of("inviter", "Alice")
            );

            NotificationDto dto2 = new NotificationDto(
                    101, userId, NotificationType.ASSIGNMENT, NotificationStatus.UNREAD, null, Map.of("taskTitle", "Implement feature X")
            );

            when(repository.findValidNotificationsForUser(userId, NotificationStatus.DISMISSED))
                    .thenReturn(List.of(notification1, notification2));

            when(notificationMapper.toNotificationDto(notification1)).thenReturn(dto1);
            when(notificationMapper.toNotificationDto(notification2)).thenReturn(dto2);

            List<NotificationDto> result = notificationService.getNotifications(userId);

            verify(repository).findValidNotificationsForUser(userId, NotificationStatus.DISMISSED);
            verify(notificationMapper).toNotificationDto(notification1);
            verify(notificationMapper).toNotificationDto(notification2);

            assertThat(result.size() == 2 && result.containsAll(List.of(dto1, dto2)));
        }

        @Test
        void shouldReturnEmptyList_WhenUserHasNoNotifications() {
            Integer userId = 10;
            when(repository.findValidNotificationsForUser(userId, NotificationStatus.DISMISSED))
                    .thenReturn(List.of());

            List<NotificationDto> result = notificationService.getNotifications(userId);

            verify(repository).findValidNotificationsForUser(userId, NotificationStatus.DISMISSED);
            verifyNoInteractions(notificationMapper);
            assertThat(result.isEmpty());
        }
    }

    @Test
    void shouldDismissInvitationNotification_WhenUpdateIsCalled() {
        InvitationUpdate invUpdate = new InvitationUpdate(55, 10, "ACCEPTED", "WORKSPACE");

        Notification existing = Notification.builder()
                .id(200)
                .userId(invUpdate.inviteeId())
                .type(NotificationType.INVITATION)
                .status(NotificationStatus.UNREAD)
                .payload(Map.of("invitationId", invUpdate.invitationId()))
                .build();

        when(repository.findByInvitationIdAndUserId(invUpdate.invitationId(), invUpdate.inviteeId(), "WORKSPACE"))
                .thenReturn(existing);

        notificationService.updateInvitationNotification(invUpdate);

        assertThat(existing.getStatus()).isEqualTo(NotificationStatus.DISMISSED);

        verify(repository).findByInvitationIdAndUserId(invUpdate.invitationId(), invUpdate.inviteeId(), "WORKSPACE");
        verify(repository).save(existing);
    }

    @Nested
    class DismissNotificationTests {
        @Test
        void shouldDismissNotification_WhenNotificationExists() {
            Integer notificationId = 42;
            Notification existing = Notification.builder()
                    .id(notificationId)
                    .status(NotificationStatus.UNREAD)
                    .build();

            when(repository.findById(notificationId)).thenReturn(Optional.of(existing));

            notificationService.dismissNotification(notificationId);

            assertThat(existing.getStatus()).isEqualTo(NotificationStatus.DISMISSED);
            verify(repository).findById(notificationId);
            verify(repository).save(existing);
        }

        @Test
        void shouldThrowException_WhenNotificationNotFound() {
            Integer missingId = 99;
            when(repository.findById(missingId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.dismissNotification(missingId))
                    .isInstanceOf(NotificationNotFoundException.class)
                    .hasMessage("Notification not found");

            verify(repository).findById(missingId);
            verify(repository, never()).save(any());
        }
    }

    @Test
    void shouldUpdateNotificationsStatusToRead() {
        List<Integer> ids = List.of(1, 2, 3);
        ReadNotificationsRequest request = new ReadNotificationsRequest(ids);

        List<Notification> notifications = List.of(
                Notification.builder().id(1).status(NotificationStatus.UNREAD).build(),
                Notification.builder().id(2).status(NotificationStatus.UNREAD).build(),
                Notification.builder().id(3).status(NotificationStatus.UNREAD).build()
        );

        when(repository.findByIdIn(ids)).thenReturn(notifications);

        notificationService.updateNotificationsStatusToRead(request);

        assertThat(notifications.stream()
                .allMatch(n -> n.getStatus() == NotificationStatus.READ))
                .isTrue();

        verify(repository).findByIdIn(ids);
        verify(repository).saveAll(notifications);
    }

    @Test
    void shouldReturnUnreadNotificationsCount() {
        Integer userId = 5;
        when(repository.getUnreadNotificationsCount(userId, NotificationStatus.UNREAD))
                .thenReturn(7);

        Integer count = notificationService.getUnreadNotificationsCount(userId);

        assertThat(count).isEqualTo(7);
        verify(repository).getUnreadNotificationsCount(userId, NotificationStatus.UNREAD);
    }
}