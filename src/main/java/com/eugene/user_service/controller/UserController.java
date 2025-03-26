package com.eugene.user_service.controller;

import com.eugene.user_service.dto.EmailDto;
import com.eugene.user_service.dto.PasswordDto;
import com.eugene.user_service.dto.RoleDto;
import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.model.User;
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
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) throws URISyntaxException {
        return userService.createUser(userDto);
    }

    @GetMapping("all_users")
    public ResponseEntity<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping
    public ResponseEntity<User> getUserById(@RequestParam String username) {
        return userService.getUserById(username);
    }

    @GetMapping("exists/{username}")
    public ResponseEntity<Boolean> isUserExist(@PathVariable String username) {
        return userService.isUserExist(username);
    }

    @PutMapping("/update/email")
    public ResponseEntity<User> updateEmail(@RequestBody EmailDto emailDto) {
        return userService.updateEmail(emailDto);
    }

    @PutMapping("/update/password")
    public ResponseEntity<User> updatePassword(@RequestBody PasswordDto passwordDto) {
        return userService.updatePassword(passwordDto);
    }

    @PutMapping("/update/role")
    public ResponseEntity<User> updateRole(@RequestBody RoleDto roleDto) {
        return userService.updateRole(roleDto);
    }

    @DeleteMapping("delete/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        return userService.deleteUser(username);
    }
}
