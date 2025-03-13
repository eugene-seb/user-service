package com.eugene.user_service.controller;

import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;
import com.eugene.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Test
    void getUser() throws Exception {
        User user = new User("String username", "String email", "String password", Role.USER);

        given(userService
                .getUserById(anyString()))
                .willReturn(ResponseEntity.ok(user));

        mockMvc
                .perform(get("/user?username={name}", anyString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username").value("String username"));

        verify(userService).getUserById(anyString());
    }
}
