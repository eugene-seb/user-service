package com.eugene.user_service.service;

import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.dto.UserInfosDto;
import com.eugene.user_service.exception.DuplicatedException;
import com.eugene.user_service.exception.NotFoundException;
import com.eugene.user_service.kafka.UserEventProducer;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserEventProducer userEventProducer;
    private final UserRepository userRepository;
    
    private static String getUserNotFoundMessage(String username) {
        return "User with username '" + username + "' not found.";
    }
    
    @Transactional
    public UserInfosDto createUser(
            UserDto userDto,
            String keycloakId
    ) {
        if (this.userRepository
                .findByUsername(userDto.getUsername())
                .isPresent()) {
            throw new DuplicatedException(
                    "User with username '" + userDto.getUsername() + "' already exists.",
                    null);
        }
        if (this.userRepository.existsById(keycloakId)) {
            throw new DuplicatedException(
                    "User with Keycloak ID '" + keycloakId + "' already exists.",
                    null);
        }
        User user;
        try {
            user = userDto.toUser(keycloakId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role specified: " + userDto.getRoles());
        }
        return this.userRepository
                .save(user)
                .toUserInfosDto();
    }
    
    @Transactional(readOnly = true)
    public List<UserInfosDto> getAllUsers() {
        return this.userRepository
                .findAll()
                .stream()
                .map(User::toUserInfosDto)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public UserInfosDto getUserByUsername(String username) {
        return this.userRepository
                .findByUsername(username)
                .map(User::toUserInfosDto)
                .orElseThrow(() -> new NotFoundException(getUserNotFoundMessage(username),
                                                         null));
    }
    
    @Transactional(readOnly = true)
    public UserInfosDto getUserById(String keycloakId) {
        return this.userRepository
                .findById(keycloakId)
                .map(User::toUserInfosDto)
                .orElseThrow(() -> new NotFoundException(getUserNotFoundMessage(keycloakId),
                                                         null));
    }
    
    @Transactional
    public void deleteUser(String keycloakId) {
        User user = this.userRepository
                .findById(keycloakId)
                .orElseThrow(() -> new NotFoundException(getUserNotFoundMessage(keycloakId),
                                                         null));
        
        this.userRepository.deleteById(keycloakId);
        this.userEventProducer.sendUserDeletedEvent(user.getReviewsIds());
    }
}
