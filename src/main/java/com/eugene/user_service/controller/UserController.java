package com.eugene.user_service.controller;

import com.eugene.user_service.dto.*;
import com.eugene.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("create_user")
    public ResponseEntity<UserInfosDto> createUser(@RequestBody UserDto userDto) throws
            URISyntaxException {
        return userService.createUser(userDto);
    }

    @GetMapping("all_users")
    public ResponseEntity<List<UserInfosDto>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping
    public ResponseEntity<UserInfosDto> getUserById(@RequestParam String username) {
        return userService.getUserById(username);
    }

    @GetMapping("exists/{username}")
    public ResponseEntity<Boolean> isUserExist(@PathVariable String username) {
        return userService.isUserExist(username);
    }

    @PutMapping("/update/email")
    public ResponseEntity<UserInfosDto> updateEmail(@RequestBody EmailDto emailDto) {
        return userService.updateEmail(emailDto);
    }

    @PutMapping("/update/password")
    public ResponseEntity<UserInfosDto> updatePassword(@RequestBody PasswordDto passwordDto) {
        return userService.updatePassword(passwordDto);
    }

    @PutMapping("/update/role")
    public ResponseEntity<UserInfosDto> updateRole(@RequestBody RoleDto roleDto) {
        return userService.updateRole(roleDto);
    }

    @DeleteMapping("delete/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        return userService.deleteUser(username);
    }
}
