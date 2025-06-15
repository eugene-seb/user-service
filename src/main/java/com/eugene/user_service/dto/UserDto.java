package com.eugene.user_service.dto;

import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;

/**
 * This class is used for the creation of a user.
 *
 * @param username
 * @param email
 * @param password
 * @param role
 */
public record UserDto(String username, String email, String password, String role) {

    public User toUser() {
        return new User(this.username(), this.email(), this.password(), Role.valueOf(this.role()));
    }
}
