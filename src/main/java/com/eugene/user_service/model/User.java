package com.eugene.user_service.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "user_table")
public class User {

    @Id
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ElementCollection
    private Set<Long> reviewsIds;

    public User() {
    }

    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Set<Long> getReviewsIds() {
        return reviewsIds;
    }

    public void setReviewsIds(Set<Long> reviewsIds) {
        this.reviewsIds = reviewsIds;
    }
}
