package com.eugene.user_service.dto.event;

import java.util.Set;

public record UserEvent(String eventType, Set<Long> reviewsIds) {
}
