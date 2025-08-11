package com.eugene.user_service.unit.controller;

import com.eugene.user_service.config.JwtConverter;
import com.eugene.user_service.controller.UserController;
import com.eugene.user_service.dto.UserInfosDto;
import com.eugene.user_service.model.Role;
import com.eugene.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest
{
    @Autowired
    MockMvc mockMvc;
    
    @MockitoBean
    JwtConverter jwtConverter;
    @MockitoBean
    UserService userService;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser() throws Exception {
        
        String keycloakId = "mock-keycloak-id";
        
        UserInfosDto user = new UserInfosDto(keycloakId,
                                             "eugene",
                                             List.of(Role.USER.name()),
                                             Set.of(1L));
        
        given(this.userService.getUserByUsername(anyString())).willReturn(user);
        
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("username").value(user.getUsername()));
        
        verify(this.userService).getUserByUsername(contains(user.getUsername()));
    }
}
