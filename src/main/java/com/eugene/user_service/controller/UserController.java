package com.eugene.user_service.controller;

import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.model.User;
import com.eugene.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("create_user")
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @GetMapping
    public ResponseEntity<User> getUserById(@RequestParam String username) {
        return userService.getUserById(username);
    }

    @GetMapping("exists/{username}")
    public ResponseEntity<Boolean> isUserExist(@PathVariable String username) {
        return userService.isUserExist(username);
    }
}
