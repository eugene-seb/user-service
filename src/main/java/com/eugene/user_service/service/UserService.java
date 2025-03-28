package com.eugene.user_service.service;

import com.eugene.user_service.dto.EmailDto;
import com.eugene.user_service.dto.PasswordDto;
import com.eugene.user_service.dto.RoleDto;
import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.kafka.UserEventProducer;
import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserEventProducer userEventProducer;
    private final UserRepository userRepository;

    public UserService(UserEventProducer userEventProducer, UserRepository userRepository) {
        this.userEventProducer = userEventProducer;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseEntity<User> createUser(UserDto userDto) throws URISyntaxException {
        try {
            Optional<User> existingUser = userRepository.findById(userDto.username());
            if (existingUser.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .build();
            }

            Role role = Role.valueOf(userDto.role());

            User user = new User(userDto.username(), userDto.email(), userDto.password(), role);
            User userCreated = userRepository.save(user);

            return ResponseEntity
                    .created(new URI("/user?username=" + userCreated.getUsername()))
                    .body(userCreated);

        } catch (IllegalArgumentException e) { // The role doesn't match
            return ResponseEntity
                    .badRequest()
                    .build();
        } catch (URISyntaxException e) {
            throw new URISyntaxException("/user?username=?", "URI failed to be created.");
        }
    }

    @Transactional
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @Transactional
    public ResponseEntity<User> getUserById(String username) {
        User user = userRepository
                .findById(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(user);
        }
    }

    @Transactional
    public ResponseEntity<Boolean> isUserExist(String username) {
        boolean exist = userRepository.existsById(username);

        return ResponseEntity.ok(exist);
    }

    @Transactional
    public ResponseEntity<User> updateEmail(EmailDto emailDto) {
        Optional<User> existingUserOpt = userRepository.findById(emailDto.username());
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
        User userOld = existingUserOpt.get();
        userOld.setEmail(emailDto.emailUpdated());

        User userUpdated = userRepository.save(userOld);

        return ResponseEntity.ok(userUpdated);
    }

    @Transactional
    public ResponseEntity<User> updatePassword(PasswordDto passwordDto) {
        Optional<User> existingUserOpt = userRepository.findById(passwordDto.username());
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
        User userOld = existingUserOpt.get();
        userOld.setPassword(passwordDto.passwordNew());

        User userUpdated = userRepository.save(userOld);

        return ResponseEntity.ok(userUpdated);
    }

    @Transactional
    public ResponseEntity<User> updateRole(RoleDto roleDto) {
        Optional<User> existingUserOpt = userRepository.findById(roleDto.username());
        if (existingUserOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        try {
            User userOld = existingUserOpt.get();
            Role role = Role.valueOf(roleDto.roleUpdated());
            userOld.setRole(role);

            User userUpdated = userRepository.save(userOld);

            return ResponseEntity.ok(userUpdated);

        } catch (IllegalArgumentException e) { // The role doesn't match
            return ResponseEntity
                    .badRequest()
                    .build();
        }
    }

    @Transactional
    public ResponseEntity<Void> deleteUser(String username) {
        userRepository
                .findById(username)
                .ifPresent(user -> {
                    userRepository.deleteById(username);
                    try {
                        userEventProducer.sendUserDeletedEvent(user.getReviewsIds());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e.getMessage(), e.getCause());
                    }
                });
        return ResponseEntity
                .ok()
                .build();
    }
}
