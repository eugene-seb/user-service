package com.eugene.user_service.dto;

import java.util.List;

public record UserDto(
        String username,
        String email,
        String password,
        String role,
        List<Integer> reviewsIds
) {
}
