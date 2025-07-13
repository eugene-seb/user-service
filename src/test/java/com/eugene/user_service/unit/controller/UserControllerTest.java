package com.eugene.user_service.unit.controller;

import com.eugene.user_service.controller.UserController;
import com.eugene.user_service.dto.UserInfosDto;
import com.eugene.user_service.model.Role;
import com.eugene.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Test
    void getUser() throws Exception {
        UserInfosDto user = new UserInfosDto("String username", "String email", Role.USER.name());

        given(userService.getUserById(anyString())).willReturn(user);

        mockMvc
                .perform(get("/user?username={name}", anyString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username").value("String username"));

        verify(userService).getUserById(anyString());
    }
}
