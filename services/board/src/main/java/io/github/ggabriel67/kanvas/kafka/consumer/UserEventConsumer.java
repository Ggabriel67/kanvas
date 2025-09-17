package io.github.ggabriel67.kanvas.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ggabriel67.kanvas.event.Event;
import io.github.ggabriel67.kanvas.event.user.UserCreated;
import io.github.ggabriel67.kanvas.event.user.UserEventType;
import io.github.ggabriel67.kanvas.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer
{
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user.events")
    public void consumeUserCreated(Event<?> event) {
        log.info("Consuming message from 'user.events' topic");
        UserEventType eventType = UserEventType.valueOf(event.getEventType());
        switch (eventType) {
            case CREATED -> {
                UserCreated userCreated = objectMapper.convertValue(event.getPayload(), UserCreated.class);
                handleUserCreated(userCreated);
            }
            case UPDATED -> log.error("Not implemented");
        }
    }

    private void handleUserCreated(UserCreated userCreated) {
        userService.saveUser(userCreated);
        log.info("User replicated successfully");
    }
}
