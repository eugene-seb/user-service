package com.eugene.user_service.kafka;

import com.eugene.user_service.dto.event.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserDeletedEvent(Set<Long> reviewsIds) {
        kafkaTemplate.send("user.events", new UserEvent("USER_DELETED", reviewsIds));
    }
}
