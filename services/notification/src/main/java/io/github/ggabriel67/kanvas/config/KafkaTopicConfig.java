package io.github.ggabriel67.kanvas.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.stereotype.Service;

@Service
public class KafkaTopicConfig
{
    @Bean
    public NewTopic columnEventsTopic() {
        return TopicBuilder.name("notification.events").build();
    }
}
