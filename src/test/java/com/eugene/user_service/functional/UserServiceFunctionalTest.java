package com.eugene.user_service.functional;

import com.eugene.user_service.dto.EmailDto;
import com.eugene.user_service.dto.PasswordDto;
import com.eugene.user_service.dto.RoleDto;
import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserServiceFunctionalTest
{
    private final UserDto userDto;
    private final EmailDto emailDto;
    private final PasswordDto passwordDto;
    private final RoleDto roleDto;

    /**
     * We don't want the context to load kafka for this test, so we mock his initialization
     * It will replace all the KafkaTemplate instances.
     */
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MockMvc mockMvc;

    public UserServiceFunctionalTest() {
        this.userDto = new UserDto("username1", "email1@gmail.com", "Password@1", Role.USER.name());
        this.emailDto = new EmailDto(this.userDto.getUsername(), "emailNew@gmail.com");
        this.passwordDto = new PasswordDto(this.userDto.getUsername(), "PasswordNew@2");
        this.roleDto = new RoleDto(this.userDto.getUsername(), Role.ADMIN.name());
    }

    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            // Enable the support of LocalDateTime for JSON serialization/deserialization)
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void createUser() throws Exception {
        this.mockMvc.perform(post("/user/create_user").contentType(MediaType.APPLICATION_JSON)
                                                      .content(asJsonString(this.userDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                                               "/user?username=" + this.userDto.getUsername()))
                    .andExpect(jsonPath("$.email").value(this.userDto.getEmail()));

        this.mockMvc.perform(post("/user/create_user").contentType(MediaType.APPLICATION_JSON)
                                                      .content(asJsonString(this.userDto)))
                    .andExpect(status().isConflict());
    }

    @Test
    @Order(2)
    void getAllUsers() throws Exception {

        this.mockMvc.perform(get("/user/all_users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(3)
    void getUserById() throws Exception {

        this.mockMvc.perform(get("/user?username={username}", this.userDto.getUsername()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
    }

    @Test
    @Order(4)
    void doesUserExist() throws Exception {

        this.mockMvc.perform(get("/user/exists/{username}", this.userDto.getUsername()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));
    }

    @Test
    @Order(5)
    void updateEmail() throws Exception {

        this.mockMvc.perform(put("/user/update/email").contentType(MediaType.APPLICATION_JSON)
                                                      .content(asJsonString(this.emailDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(this.emailDto.getEmail()));
    }

    @Test
    @Order(6)
    void updatePassword() throws Exception {

        this.mockMvc.perform(put("/user/update/password").contentType(MediaType.APPLICATION_JSON)
                                                         .content(asJsonString(this.passwordDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(this.passwordDto.getUsername()));
    }

    @Test
    @Order(7)
    void updateRole() throws Exception {

        this.mockMvc.perform(put("/user/update/role").contentType(MediaType.APPLICATION_JSON)
                                                     .content(asJsonString(this.roleDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value(this.roleDto.getRole()));
    }

    @Test
    @Order(8)
    void deleteUser() throws Exception {

        this.mockMvc.perform(delete("/user/delete/{username}", this.userDto.getUsername()))
                    .andExpect(status().isOk());

        this.mockMvc.perform(get("/user?username={username}", this.userDto.getUsername()))
                    .andExpect(status().isNotFound());
    }
}
