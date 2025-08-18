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
    @Column(name = "keycloak_id", unique = true, nullable = false)
    private String keycloakId;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "keycloak_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    
    @ElementCollection
    private Set<Long> reviewsIds = new HashSet<>();
    
    public User(
            String keycloakId,
            String username,
            Set<Role> roles
    ) {
        this.keycloakId = keycloakId;
        this.username = username;
        this.roles = roles;
    }
    
    public UserInfosDto toUserInfosDto() {
        return new UserInfosDto(this.keycloakId,
                                this.username,
                                this.roles
                                        .stream()
                                        .map(Enum::name)
                                        .toList(),
                                this.reviewsIds);
    }
}
