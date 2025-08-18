package com.eugene.user_service.repository;

import com.eugene.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, String>
{
    Optional<User> findByUsername(String username);
}
