package com.eugene.user_service.controller;

import com.eugene.user_service.config.JwtConverter;
import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.dto.UserInfosDto;
import com.eugene.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;
    private final JwtConverter jwtConverter;
    
    @Operation(summary = "Create a new user profile after Keycloak signup")
    @PostMapping("/create/profile")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MODERATOR')")
    public ResponseEntity<UserInfosDto> createProfile(
            @Valid @RequestBody UserDto userDto,
            @AuthenticationPrincipal Jwt jwt
    ) throws URISyntaxException {
        
        userDto.setRoles((Set<String>) jwtConverter.extractKeycloakRoles(jwt));
        
        String keycloakId = jwt.getClaim("sub");
        UserInfosDto userCreated = userService.createUser(userDto,
                                                          keycloakId);
        
        return ResponseEntity
                .created(new URI("/api/user/" + keycloakId))
                .body(userCreated);
    }
    
    @Operation(summary = "Get all users (ADMIN only)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserInfosDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfosDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getClaim("sub");
        return ResponseEntity.ok(userService.getUserById(keycloakId));
    }
    
    @Operation(summary = "Get user by Keycloak ID (ADMIN only)")
    @GetMapping("/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfosDto> getUserById(@PathVariable String keycloakId) {
        return ResponseEntity.ok(userService.getUserById(keycloakId));
    }
    
    @Operation(summary = "Get user by username (ADMIN only)")
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserInfosDto> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
    
    @Operation(summary = "Check if a user is present in the DB. (ADMIN only)")
    @GetMapping("exists/{keycloakId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isUserExist(@PathVariable String keycloakId) {
        return ResponseEntity.ok(this.userService.isUserExist(keycloakId));
    }
    
    @Operation(summary = "Delete current user account.")
    @DeleteMapping("/delete/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getClaim("sub");
        userService.deleteUser(keycloakId);
        return ResponseEntity
                .noContent()
                .build();
    }
}
