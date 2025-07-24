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
public class UserService
{
    private final UserEventProducer userEventProducer;
    private final UserRepository userRepository;

    public UserService(
            UserEventProducer userEventProducer,
            UserRepository userRepository
    ) {
        this.userEventProducer = userEventProducer;
        this.userRepository = userRepository;
    }

    private static String getUserNotFoundMessage(String username) {
        return "User with username '" + username + "' not found.";
    }

    @Transactional
    public UserInfosDto createUser(UserDto userDto) {
        boolean exists = this.userRepository.findById(userDto.getUsername())
                                            .isPresent();
        if (exists) {
            throw new DuplicatedException(
                    "User with username '" + userDto.getUsername() + "' already exists.", null);
        }
        try {
            User user = userDto.toUser();
            return this.userRepository.save(user)
                                      .toUserInfosDto();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role specified: " + userDto.getRole());
        }
    }

    @Transactional
    public List<UserInfosDto> getAllUsers() {
        return this.userRepository.findAll()
                                  .stream()
                                  .map(User::toUserInfosDto)
                                  .toList();
    }

    @Transactional
    public UserInfosDto getUserById(String username) {
        return this.userRepository.findById(username)
                                  .map(User::toUserInfosDto)
                                  .orElseThrow(() -> new NotFoundException(
                                          getUserNotFoundMessage(username), null));
    }

    @Transactional
    public Boolean isUserExist(String username) {
        return this.userRepository.existsById(username);
    }

    @Transactional
    public UserInfosDto updateEmail(EmailDto emailDto) {
        User user = this.userRepository.findById(emailDto.getUsername())
                                       .orElseThrow(() -> new NotFoundException(
                                               getUserNotFoundMessage(emailDto.getUsername()),
                                               null));

        user.setEmail(emailDto.getEmail());

        return this.userRepository.save(user)
                                  .toUserInfosDto();
    }

    @Transactional
    public UserInfosDto updatePassword(PasswordDto passwordDto) {
        User user = this.userRepository.findById(passwordDto.getUsername())
                                       .orElseThrow(() -> new NotFoundException(
                                               getUserNotFoundMessage(passwordDto.getUsername()),
                                               null));

        user.setPassword(passwordDto.getPassword());

        return this.userRepository.save(user)
                                  .toUserInfosDto();
    }

    @Transactional
    public UserInfosDto updateRole(RoleDto roleDto) {
        User user = this.userRepository.findById(roleDto.getUsername())
                                       .orElseThrow(() -> new NotFoundException(
                                               getUserNotFoundMessage(roleDto.getUsername()),
                                               null));
        try {
            Role role = Role.valueOf(roleDto.getRole());
            user.setRole(role);

            return this.userRepository.save(user)
                                      .toUserInfosDto();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role value: " + roleDto.getRole());
        }
    }

    @Transactional
    public void deleteUser(String username) {
        User user = this.userRepository.findById(username)
                                       .orElseThrow(() -> new NotFoundException(
                                               getUserNotFoundMessage(username), null));

        this.userRepository.deleteById(username);
        this.userEventProducer.sendUserDeletedEvent(user.getReviewsIds());
    }
}
