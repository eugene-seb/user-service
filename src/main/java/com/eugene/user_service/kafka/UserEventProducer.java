package com.eugene.user_service.kafka;

import com.eugene.user_service.dto.event.UserDtoEvent;
import com.eugene.user_service.exception.JsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserDeletedEvent(Set<Long> reviewsIds) {
        try {
            String json = objectMapper.writeValueAsString(
                    new UserDtoEvent(KafkaEventType.USER_DELETED, reviewsIds));
            kafkaTemplate.send("user.events", json);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize the list of IDs", e.getCause());
        }
    }
}
