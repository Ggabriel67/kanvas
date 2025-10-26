package io.github.ggabriel67.kanvas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.kafka.producer.NotificationEventProducer;
import io.github.ggabriel67.kanvas.notification.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerIntegrationTest
{
    @MockitoBean
    private NotificationEventProducer notificationEventProducer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void cleanDatabase() {
        notificationRepository.deleteAll();
    }

    @Test
    void getNotifications_ShouldReturnNotificationsList() throws Exception {
        Integer userId = 10;

        Notification n1 = Notification.builder().userId(userId).type(NotificationType.ASSIGNMENT).status(NotificationStatus.READ)
                .payload(Map.of(
                        "taskTitle", "Test Task",
                        "boardName", "Test Board",
                        "assigned", true
                ))
                .build();
        Notification n2 = Notification.builder().userId(userId).type(NotificationType.REMOVED_FROM_BOARD).status(NotificationStatus.UNREAD)
                .payload(Map.of(
                        "boardName", "Board"
                ))
                .build();
        Notification n3 = Notification.builder().userId(userId).type(NotificationType.INVITATION).status(NotificationStatus.DISMISSED).build();
        notificationRepository.saveAll(List.of(n1, n2, n3));

        var result = mockMvc.perform(get("/api/v1/notifications/{userId}", userId))
                .andExpect(status().isOk())
                .andReturn();

        var notificationDtos = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<NotificationDto>>() {});

        Map<Integer, NotificationDto> dtoMap = notificationDtos.stream()
                .collect(Collectors.toMap(NotificationDto::notificationId, Function.identity()));

        assertThat(dtoMap.get(n1.getId()).type()).isEqualTo(NotificationType.ASSIGNMENT);
        assertThat(dtoMap.get(n1.getId()).userId()).isEqualTo(userId);
        assertThat(dtoMap.get(n1.getId()).status()).isEqualTo(NotificationStatus.READ);
        assertThat(dtoMap.get(n1.getId()).payload().get("taskTitle")).isEqualTo("Test Task");
        assertThat(dtoMap.get(n1.getId()).payload().get("boardName")).isEqualTo("Test Board");
        assertThat(dtoMap.get(n1.getId()).payload().get("assigned")).isEqualTo(true);

        assertThat(dtoMap.get(n2.getId()).type()).isEqualTo(NotificationType.REMOVED_FROM_BOARD);
        assertThat(dtoMap.get(n2.getId()).userId()).isEqualTo(userId);
        assertThat(dtoMap.get(n2.getId()).status()).isEqualTo(NotificationStatus.UNREAD);
        assertThat(dtoMap.get(n2.getId()).payload().get("boardName")).isEqualTo("Board");

        assertThat(dtoMap.get(n3.getId())).isNull();
    }

    @Test
    void dismissNotification_ShouldDismissNotificationSuccessfully() throws Exception {
        Notification notification = Notification.builder().userId(10).type(NotificationType.ASSIGNMENT).status(NotificationStatus.READ).build();
        notificationRepository.save(notification);

        mockMvc.perform(patch("/api/v1/notifications/{notificationId}/dismiss", notification.getId()))
                .andExpect(status().isOk());

        var dismissedNotification = notificationRepository.findById(notification.getId()).get();
        assertThat(dismissedNotification.getStatus()).isEqualTo(NotificationStatus.DISMISSED);
    }

    @Test
    void updateNotificationsStatusToRead_ShouldUpdateStatusesSuccessfully() throws Exception {
        Notification n1 = Notification.builder().type(NotificationType.ASSIGNMENT).status(NotificationStatus.UNREAD).build();
        Notification n2 = Notification.builder().type(NotificationType.REMOVED_FROM_BOARD).status(NotificationStatus.UNREAD).build();
        notificationRepository.saveAll(List.of(n1, n2));

        ReadNotificationsRequest request = new ReadNotificationsRequest(
                List.of(n1.getId(), n2.getId())
        );

        mockMvc.perform(patch("/api/v1/notifications/status/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        var updatedNotification1 = notificationRepository.findById(n1.getId()).get();
        var updatedNotification2 = notificationRepository.findById(n2.getId()).get();

        assertThat(updatedNotification1.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(updatedNotification2.getStatus()).isEqualTo(NotificationStatus.READ);
    }
}
