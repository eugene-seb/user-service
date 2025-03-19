package com.eugene.user_service.mocktest;

import com.eugene.user_service.dto.EmailDto;
import com.eugene.user_service.dto.PasswordDto;
import com.eugene.user_service.dto.RoleDto;
import com.eugene.user_service.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserMockMcvTest {

    private final UserDto userDto;
    private final EmailDto emailDto;
    private final PasswordDto passwordDto;
    private final RoleDto roleDto;

    @Autowired
    private MockMvc mockMvc;

    public UserMockMcvTest() {
        this.userDto = new UserDto("username1", "email1", "password1", "USER", List.of());
        this.emailDto = new EmailDto(this.userDto.username(), "emailNew");
        this.passwordDto = new PasswordDto(this.userDto.username(), "passwordNew");
        this.roleDto = new RoleDto(this.userDto.username(), "ADMIN");
    }

    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void createUser() throws Exception {
        mockMvc
                .perform(post("/user/create_user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(this.userDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/user?username=" + this.userDto.username()))
                .andExpect(jsonPath("$.email").value(this.userDto.email()));

        mockMvc
                .perform(post("/user/create_user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(this.userDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(2)
    void getAllUsers() throws Exception {

        mockMvc
                .perform(get("/user/all_users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(3)
    void getUserById() throws Exception {

        mockMvc
                .perform(get("/user?username={username}", this.userDto.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(this.userDto.username()));
    }

    @Test
    @Order(4)
    void doesUserExist() throws Exception {

        mockMvc
                .perform(get("/user/exists/{username}", userDto.username()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @Order(5)
    void updateEmail() throws Exception {

        mockMvc
                .perform(put("/user/update/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(emailDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(emailDto.emailUpdated()));
    }

    @Test
    @Order(6)
    void updatePassword() throws Exception {

        mockMvc
                .perform(put("/user/update/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(passwordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value(passwordDto.passwordNew()));
    }

    @Test
    @Order(7)
    void updateRole() throws Exception {

        mockMvc
                .perform(put("/user/update/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(roleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(roleDto.roleUpdated()));
    }

    @Test
    @Order(8)
    void deleteUser() throws Exception {

        mockMvc
                .perform(delete("/user/delete/{username}", this.userDto.username()))
                .andExpect(status().isOk());

        mockMvc
                .perform(get("/user?username={username}", this.userDto.username()))
                .andExpect(status().isNotFound());
    }
}
