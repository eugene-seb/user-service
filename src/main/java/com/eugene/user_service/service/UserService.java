package com.eugene.user_service.service;

import com.eugene.user_service.dto.*;
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

@Service
public class UserService {
    private final UserEventProducer userEventProducer;
    private final UserRepository userRepository;

    public UserService(UserEventProducer userEventProducer, UserRepository userRepository) {
        this.userEventProducer = userEventProducer;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseEntity<UserInfosDto> createUser(UserDto userDto) throws URISyntaxException {
        try {
            boolean isUserExists = userRepository
                    .findById(userDto.username())
                    .isPresent();
            if (isUserExists) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .build();
            } else {
                User user = userDto.toUser();
                UserInfosDto userCreated = userRepository
                        .save(user)
                        .toUserInfosDto();

                return ResponseEntity
                        .created(new URI("/user?username=" + userCreated.username()))
                        .body(userCreated);
            }
        } catch (IllegalArgumentException e) { // The role doesn't match
            return ResponseEntity
                    .badRequest()
                    .build();
        } catch (URISyntaxException e) {
            throw new URISyntaxException("/user?username=?", "URI failed to be created.");
        }
    }

    @Transactional
    public ResponseEntity<List<UserInfosDto>> getAllUsers() {
        List<UserInfosDto> users = userRepository
                .findAll()
                .stream()
                .map(User::toUserInfosDto)
                .toList();
        return ResponseEntity.ok(users);
    }

    @Transactional
    public ResponseEntity<UserInfosDto> getUserById(String username) {
        User user = userRepository
                .findById(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(user.toUserInfosDto());
        }
    }

    @Transactional
    public ResponseEntity<Boolean> isUserExist(String username) {
        boolean exist = userRepository.existsById(username);

        return ResponseEntity.ok(exist);
    }

    @Transactional
    public ResponseEntity<UserInfosDto> updateEmail(EmailDto emailDto) {
        User user = userRepository
                .findById(emailDto.username())
                .orElse(null);
        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            user.setEmail(emailDto.emailUpdated());

            User userUpdated = userRepository.save(user);

            return ResponseEntity.ok(userUpdated.toUserInfosDto());
        }
    }

    @Transactional
    public ResponseEntity<UserInfosDto> updatePassword(PasswordDto passwordDto) {
        User user = userRepository
                .findById(passwordDto.username())
                .orElse(null);
        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            user.setPassword(passwordDto.passwordNew());

            UserInfosDto userUpdated = userRepository
                    .save(user)
                    .toUserInfosDto();

            return ResponseEntity.ok(userUpdated);
        }
    }

    @Transactional
    public ResponseEntity<UserInfosDto> updateRole(RoleDto roleDto) {
        User user = userRepository
                .findById(roleDto.username())
                .orElse(null);
        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            try {
                Role role = Role.valueOf(roleDto.roleUpdated());
                user.setRole(role);

                UserInfosDto userUpdated = userRepository
                        .save(user)
                        .toUserInfosDto();

                return ResponseEntity.ok(userUpdated);

            } catch (IllegalArgumentException e) { // The role doesn't match
                return ResponseEntity
                        .badRequest()
                        .build();
            }
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
