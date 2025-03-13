package com.eugene.user_service.service;

import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<User> createUser(UserDto userDto) {
        try {
            Role role = Role.valueOf(userDto.role());
            User user = new User(userDto.username(), userDto.email(), userDto.password(), role);
            return ResponseEntity.ok(userRepository
                    .save(user));
        } catch (IllegalArgumentException e) { // The role doesn't match
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    public ResponseEntity<User> getUserById(String username) {
        User user = userRepository
                .findById(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity
                    .ok(user);
        }
    }

    public ResponseEntity<Boolean> isUserExist(String username) {
        boolean exist = userRepository.existsById(username);

        return ResponseEntity.ok(exist);
    }
}
