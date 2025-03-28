package com.eugene.user_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookTopic() {
        return new NewTopic("book.events", 1, (short) 1);
    }

    @Bean
    public NewTopic reviewTopic() {
        return new NewTopic("review.events", 1, (short) 1);
    }
}