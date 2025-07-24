package com.eugene.user_service.controller;

import com.eugene.user_service.dto.*;
import com.eugene.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController
{
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("create_user")
    public ResponseEntity<UserInfosDto> createUser(@Valid @RequestBody UserDto userDto)
            throws URISyntaxException {
        UserInfosDto userCreated = this.userService.createUser(userDto);

        return ResponseEntity.created(new URI("/user?username=" + userCreated.getUsername()))
                             .body(userCreated);
    }

    @GetMapping("all_users")
    public ResponseEntity<List<UserInfosDto>> getAllUsers() {
        return ResponseEntity.ok(this.userService.getAllUsers());
    }

    @GetMapping
    public ResponseEntity<UserInfosDto> getUserById(@RequestParam String username) {
        return ResponseEntity.ok(this.userService.getUserById(username));
    }

    @GetMapping("exists/{username}")
    public ResponseEntity<Boolean> isUserExist(@PathVariable String username) {
        return ResponseEntity.ok(this.userService.isUserExist(username));
    }

    @PutMapping("/update/email")
    public ResponseEntity<UserInfosDto> updateEmail(@Valid @RequestBody EmailDto emailDto) {
        return ResponseEntity.ok(this.userService.updateEmail(emailDto));
    }

    @PutMapping("/update/password")
    public ResponseEntity<UserInfosDto> updatePassword(@Valid @RequestBody PasswordDto passwordDto) {
        return ResponseEntity.ok(this.userService.updatePassword(passwordDto));
    }

    @PutMapping("/update/role")
    public ResponseEntity<UserInfosDto> updateRole(@Valid @RequestBody RoleDto roleDto) {
        return ResponseEntity.ok(this.userService.updateRole(roleDto));
    }

    @DeleteMapping("delete/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        this.userService.deleteUser(username);
        return ResponseEntity.ok()
                             .build();
    }
}
