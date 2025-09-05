package io.github.ggabriel67.user.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaUserTopicConfig
{
    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name("user-topic").build();
    }
}
