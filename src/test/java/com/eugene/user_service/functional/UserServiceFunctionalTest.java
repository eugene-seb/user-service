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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserControllerFunctionalTest
{
    private final UserDto userDto;
    private final UserDto userDto2;
    private final String keycloakId = "mock-keycloak-id";
    private final String keycloakId2 = "mock-keycloak-id-2";
    
    /**
     * We don't want the context to load kafka for this test, so we mock his initialization
     * It will replace all the KafkaTemplate instances.
     */
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private MockMvc mockMvc;
    
    public UserControllerFunctionalTest() {
        this.userDto = new UserDto("eugene",
                                   Set.of(Role.ADMIN.name()));
        this.userDto2 = new UserDto("john",
                                    Set.of(Role.USER.name()));
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
    
    private Jwt createJwt(
            String keycloakId,
            Set<String> roles
    ) {
        return Jwt
                .withTokenValue("token")
                .header("alg",
                        "none")
                .claim("sub",
                       keycloakId)
                .claim("roles",
                       roles)
                .build();
    }
    
    @Test
    @Order(1)
    void createProfile_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.userDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(2)
    @WithMockUser(roles = {"USER"})
    void createProfile_withUserRole_shouldSucceed() throws Exception {
        Jwt jwt = createJwt(keycloakId,
                            Set.of("USER"));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.userDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/user/" + keycloakId))
                .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
    }
    
    @Test
    @Order(3)
    @WithMockUser(roles = {"USER"})
    void createProfile_withDuplicateUsername_shouldReturnConflict() throws Exception {
        Jwt jwt = createJwt(keycloakId2,
                            Set.of("USER"));
        
        // Try to create user with same username but different keycloak ID
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.userDto)) // Same username as previous test
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(4)
    @WithMockUser(roles = {"USER"})
    void createProfile_withDuplicateKeycloakId_shouldReturnConflict() throws Exception {
        Jwt jwt = createJwt(keycloakId,
                            Set.of("USER")); // Same keycloak ID as first test
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.userDto2))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(5)
    @WithMockUser(roles = {"USER"})
    void createProfile_withInvalidRole_shouldReturnBadRequest() throws Exception {
        Jwt jwt = createJwt("new-keycloak-id",
                            Set.of("USER"));
        UserDto invalidUserDto = new UserDto("newuser",
                                             Set.of("INVALID_ROLE"));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidUserDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(6)
    void getAllUsers_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/user/all"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(7)
    @WithMockUser(roles = {"USER"})
    void getAllUsers_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(get("/api/user/all"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(8)
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_withAdminRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value(this.userDto.getUsername()));
    }
    
    @Test
    @Order(9)
    void getCurrentUser_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(10)
    @WithMockUser
    void getCurrentUser_withAuthenticatedUser_shouldSucceed() throws Exception {
        Jwt jwt = createJwt(keycloakId,
                            Set.of("USER",
                                   "ADMIN"));
        
        this.mockMvc
                .perform(get("/api/user/me").with(SecurityMockMvcRequestPostProcessors
                                                          .jwt()
                                                          .jwt(jwt)
                                                          .authorities(new SimpleGrantedAuthority("ROLE_USER"),
                                                                       new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
    }
    
    @Test
    @Order(11)
    @WithMockUser
    void getCurrentUser_withNonExistingUser_shouldReturnNotFound() throws Exception {
        Jwt jwt = createJwt("non-existing-keycloak-id",
                            Set.of("USER"));
        
        this.mockMvc
                .perform(get("/api/user/me").with(SecurityMockMvcRequestPostProcessors
                                                          .jwt()
                                                          .jwt(jwt)
                                                          .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(12)
    @WithMockUser(roles = {"USER"})
    void getUserById_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(get("/api/user/{keycloakId}",
                             keycloakId))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(13)
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_withAdminRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/user/{keycloakId}",
                             keycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
    }
    
    @Test
    @Order(14)
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/user/{keycloakId}",
                             "non-existing-id"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(15)
    @WithMockUser(roles = {"USER"})
    void getUserByUsername_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             this.userDto.getUsername()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(16)
    @WithMockUser(roles = {"ADMIN"})
    void getUserByUsername_withAdminRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             this.userDto.getUsername()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(this.userDto.getUsername()));
    }
    
    @Test
    @Order(17)
    @WithMockUser(roles = {"ADMIN"})
    void getUserByUsername_withNonExistingUsername_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             "non-existing-username"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(18)
    void isUserExist_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakId}",
                             keycloakId))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(19)
    @WithMockUser
    void isUserExist_withExistingUser_shouldReturnTrue() throws Exception {
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakId}",
                             keycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
    
    @Test
    @Order(20)
    @WithMockUser
    void isUserExist_withNonExistingUser_shouldReturnFalse() throws Exception {
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakId}",
                             "non-existing-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
    
    @Test
    @Order(21)
    void deleteCurrentUser_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(delete("/api/user/delete/me").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(22)
    @WithMockUser
    void deleteCurrentUser_withAuthenticatedUser_shouldSucceed() throws Exception {
        // First create another user to delete
        Jwt jwt = createJwt(keycloakId2,
                            Set.of("USER"));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.userDto2))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isCreated());
        
        // Now delete the user
        this.mockMvc
                .perform(delete("/api/user/delete/me")
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isNoContent());
        
        // Verify user is deleted
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakId}",
                             keycloakId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
    
    @Test
    @Order(23)
    @WithMockUser
    void deleteCurrentUser_withNonExistingUser_shouldReturnNotFound() throws Exception {
        Jwt jwt = createJwt("non-existing-keycloak-id",
                            Set.of("USER"));
        
        this.mockMvc
                .perform(delete("/api/user/delete/me")
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(24)
    @WithMockUser(roles = {"USER"})
    void createProfile_withBlankUsername_shouldReturnBadRequest() throws Exception {
        Jwt jwt = createJwt("new-keycloak-id",
                            Set.of("USER"));
        UserDto invalidUserDto = new UserDto("",
                                             Set.of("USER"));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidUserDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(25)
    @WithMockUser(roles = {"USER"})
    void createProfile_withNullUsername_shouldReturnBadRequest() throws Exception {
        Jwt jwt = createJwt("new-keycloak-id",
                            Set.of("USER"));
        UserDto invalidUserDto = new UserDto(null,
                                             Set.of("USER"));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidUserDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}