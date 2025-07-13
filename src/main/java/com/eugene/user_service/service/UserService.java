package com.eugene.user_service.service;

import com.eugene.user_service.dto.*;
import com.eugene.user_service.exception.DuplicatedException;
import com.eugene.user_service.exception.NotFoundException;
import com.eugene.user_service.kafka.UserEventProducer;
import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserEventProducer userEventProducer;
    private final UserRepository userRepository;

    public UserService(UserEventProducer userEventProducer, UserRepository userRepository) {
        this.userEventProducer = userEventProducer;
        this.userRepository = userRepository;
    }

    private static String getUserNotFoundMessage(String username) {
        return "User with username '" + username + "' not found.";
    }

    @Transactional
    public UserInfosDto createUser(UserDto userDto) {
        boolean exists = userRepository
                .findById(userDto.username())
                .isPresent();
        if (exists) {
            throw new DuplicatedException(
                    "User with username '" + userDto.username() + "' already exists.", null);
        }
        try {
            User user = userDto.toUser();
            return userRepository
                    .save(user)
                    .toUserInfosDto();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role specified: " + userDto.role());
        }
    }

    @Transactional
    public List<UserInfosDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(User::toUserInfosDto)
                .toList();
    }

    @Transactional
    public UserInfosDto getUserById(String username) {
        return userRepository
                .findById(username)
                .map(User::toUserInfosDto)
                .orElseThrow(() -> new NotFoundException(getUserNotFoundMessage(username), null));
    }

    @Transactional
    public Boolean isUserExist(String username) {
        return userRepository.existsById(username);
    }

    @Transactional
    public UserInfosDto updateEmail(EmailDto emailDto) {
        User user = userRepository
                .findById(emailDto.username())
                .orElseThrow(
                        () -> new NotFoundException(getUserNotFoundMessage(emailDto.username()),
                                null));

        user.setEmail(emailDto.emailUpdated());

        return userRepository
                .save(user)
                .toUserInfosDto();
    }

    @Transactional
    public UserInfosDto updatePassword(PasswordDto passwordDto) {
        User user = userRepository
                .findById(passwordDto.username())
                .orElseThrow(
                        () -> new NotFoundException(getUserNotFoundMessage(passwordDto.username()),
                                null));

        user.setPassword(passwordDto.passwordNew());

        return userRepository
                .save(user)
                .toUserInfosDto();
    }

    @Transactional
    public UserInfosDto updateRole(RoleDto roleDto) {
        User user = userRepository
                .findById(roleDto.username())
                .orElseThrow(() -> new NotFoundException(getUserNotFoundMessage(roleDto.username()),
                        null));
        try {
            Role role = Role.valueOf(roleDto.roleUpdated());
            user.setRole(role);

            return userRepository
                    .save(user)
                    .toUserInfosDto();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role value: " + roleDto.roleUpdated());
        }
    }

    @Transactional
    public void deleteUser(String username) {
        User user = userRepository
                .findById(username)
                .orElseThrow(() -> new NotFoundException(getUserNotFoundMessage(username), null));

        userRepository.deleteById(username);
        userEventProducer.sendUserDeletedEvent(user.getReviewsIds());
    }
}
