package com.eugene.user_service.dto;

/**
 * This class is used to transfer simple user information.
 *
 * @param username the username of the user
 * @param email    the email of the user
 * @param role     the role of the user
 */
public record UserInfosDto(String username, String email, String role) {
}
