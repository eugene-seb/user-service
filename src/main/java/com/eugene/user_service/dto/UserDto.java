package com.eugene.user_service.dto;

import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @NotBlank(message = "The email is required.")
    @Email(message = "The email is not a valid email.")
    private String email;

    @NotBlank(message = "The password is required.")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()]).{8,}$", message =
            "Password should be be at least 8 characters long and combination of uppercase " +
            "letters, lowercase letters, numbers, and special characters -- @#$%^&+=!*() --")
    private String password;

    @NotBlank(message = "The role is required.")
    private String role;

    public User toUser() {
        return new User(this.username, this.email, this.password, Role.valueOf(this.role));
    }
}
