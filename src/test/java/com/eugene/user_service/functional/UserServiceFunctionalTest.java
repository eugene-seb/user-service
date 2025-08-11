package com.eugene.user_service.functional;

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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

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
    private final String keycloakId = "mock-keycloak-id";
    
    /**
     * We don't want the context to load kafka for this test, so we mock his initialization
     * It will replace all the KafkaTemplate instances.
     */
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private MockMvc mockMvc;
    
    public UserServiceFunctionalTest() {
        this.userDto = new UserDto("eugene",
                                   Set.of(Role.ADMIN.name()));
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
    @WithMockUser(roles = "ADMIN")
    void createUser() throws Exception {
        Jwt jwt = Jwt
                .withTokenValue("token")
                .header("alg",
                        "none")
                .claim("sub",
                       keycloakId)
                .claim("roles",
                       Set.of("USER",
                              "ADMIN",
                              "MODERATOR"))
                .build();
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.userDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER"),
                                                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                                                            new SimpleGrantedAuthority("ROLE_MODERATOR"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/user/" + keycloakId))
                .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.userDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER"),
                                                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                                                            new SimpleGrantedAuthority("ROLE_MODERATOR"))))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(2)
    @WithMockUser(roles = "ADMIN")
    void getAllUsers() throws Exception {
        
        this.mockMvc
                .perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
    
    @Test
    @Order(3)
    @WithMockUser(roles = "ADMIN")
    void getUserById() throws Exception {
        
        this.mockMvc
                .perform(get("/api/user/{id}",
                             keycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
    }
    
    @Test
    @Order(3)
    @WithMockUser(roles = "ADMIN")
    void getUserByUsername() throws Exception {
        
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             this.userDto.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
    }
    
    @Test
    @Order(4)
    @WithMockUser
    void doesUserExist() throws Exception {
        
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakID}",
                             keycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
    
    @Test
    @Order(5)
    @WithMockUser(roles = "ADMIN")
    void deleteUser() throws Exception {
        Jwt jwt = Jwt
                .withTokenValue("token")
                .header("alg",
                        "none")
                .claim("sub",
                       keycloakId)
                .claim("roles",
                       Set.of("ADMIN"))
                .build();
        
        this.mockMvc
                .perform(delete("/api/user/delete/me").with(SecurityMockMvcRequestPostProcessors
                                                                    .jwt()
                                                                    .jwt(jwt)
                                                                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
        
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             this.userDto.getUsername()))
                .andExpect(status().isNotFound());
    }
}
