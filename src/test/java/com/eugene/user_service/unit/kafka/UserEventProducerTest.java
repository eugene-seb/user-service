package com.eugene.user_service.unit.kafka;

import com.eugene.user_service.dto.event.UserDtoEvent;
import com.eugene.user_service.kafka.KafkaEventType;
import com.eugene.user_service.kafka.UserEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserEventProducerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks
    private UserEventProducer userEventProducer;

    @Test
    void sendUserDeletedEvent() throws JsonProcessingException {
        Set<Long> reviewsIds = Set.of(1L, 2L);
        String json = objectMapper.writeValueAsString(
                new UserDtoEvent(KafkaEventType.USER_DELETED, reviewsIds));

        userEventProducer.sendUserDeletedEvent(reviewsIds);

        verify(kafkaTemplate, times(1)).send("user.events", json);
    }
}
