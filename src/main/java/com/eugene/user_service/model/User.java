package com.eugene.user_service.model;

import com.eugene.user_service.dto.UserInfosDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_table")
@Setter
@Getter
@NoArgsConstructor
public class User
{
    @Id
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection
    private Set<Long> reviewsIds;

    public User(
            String username,
            String email,
            String password,
            Role role
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.reviewsIds = new HashSet<>();
    }

    public UserInfosDto toUserInfosDto() {
        return new UserInfosDto(this.getUsername(), this.getEmail(), this.getRole()
                                                                         .name());
    }

}
