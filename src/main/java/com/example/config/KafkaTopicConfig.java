package com.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${topic.order}")
    private String orderTopic;
    @Value("${topic.order-dlq}")
    private String orderTopicDLQ;

    @Bean
    public NewTopic orderTopic() {
        return new NewTopic(orderTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic orderTopicDLQ() {
        return new NewTopic(orderTopicDLQ, 3, (short) 1);
    }
}
