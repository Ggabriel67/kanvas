package io.github.ggabriel67.user.kafka;

import io.github.ggabriel67.user.user.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProducer
{
    private final KafkaTemplate<String, UserDto> kafkaTemplate;

    public void sendUserReplica(UserDto userDto) {
        log.info("Sending user replica");
        Message<UserDto> message = MessageBuilder
                .withPayload(userDto)
                .setHeader(KafkaHeaders.TOPIC, "user-topic")
                .build();
        kafkaTemplate.send(message);
    }
}
