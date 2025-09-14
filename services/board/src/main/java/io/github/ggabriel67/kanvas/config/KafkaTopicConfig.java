package io.github.ggabriel67.kanvas.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig
{
    @Bean
    public NewTopic invitationEventsTopic() {
        return TopicBuilder.name("invitation.events").build();
    }

    @Bean
    public NewTopic boardEventsTopic() {
        return TopicBuilder.name("board.events").build();
    }
}
