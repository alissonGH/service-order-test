package com.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${topic.order}")
    private String topicName;

    @Bean
    public NewTopic orderTopic() {
        return new NewTopic(topicName, 3, (short) 1);
    }
}
