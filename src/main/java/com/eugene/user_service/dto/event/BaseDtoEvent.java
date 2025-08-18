package com.eugene.user_service.dto.event;

import com.eugene.user_service.kafka.KafkaEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
class BaseDtoEvent {
    private KafkaEventType eventType;

    public BaseDtoEvent(KafkaEventType eventType) {
        this.eventType = eventType;
    }
}
