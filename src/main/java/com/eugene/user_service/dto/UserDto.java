package com.eugene.user_service.dto;

import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is used for the creation of a user.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto
{
    @NotBlank(message = "The username is required.")
    @Size(min = 3, max = 20, message = "The username must be from 3 to 20 characters.")
    private String username;
    
    @NotEmpty(message = "Roles are required.")
    private Set<String> roles;
    
    public User toUser(String keycloakId) {
        Set<Role> userRoles = this.roles
                .stream()
                .filter(role -> Arrays
                        .stream(Role.values())
                        .anyMatch(r -> r
                                .name()
                                .equals(role)))
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        
        if (userRoles.isEmpty()) {
            throw new IllegalArgumentException("Invalid role specified: " + this.roles);
        }
        
        return new User(keycloakId,
                        this.username,
                        userRoles);
    }
    
}
