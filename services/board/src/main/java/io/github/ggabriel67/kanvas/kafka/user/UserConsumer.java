package io.github.ggabriel67.kanvas.kafka.user;

import io.github.ggabriel67.kanvas.user.UserDto;
import io.github.ggabriel67.kanvas.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConsumer
{
    private final UserService userService;

    @KafkaListener(topics = "user.created")
    public void consumeUserCreated(UserDto userDto) {
        log.info("Consuming message from 'user.created' topic");
        userService.saveUser(userDto);
    }
}
