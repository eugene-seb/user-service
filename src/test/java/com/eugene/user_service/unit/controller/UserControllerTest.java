package com.eugene.user_service.unit.controller;

import com.eugene.user_service.config.JwtConverter;
import com.eugene.user_service.config.SecurityConfig;
import com.eugene.user_service.controller.UserController;
import com.eugene.user_service.dto.UserDto;
import com.eugene.user_service.dto.UserInfosDto;
import com.eugene.user_service.exception.DuplicatedException;
import com.eugene.user_service.exception.NotFoundException;
import com.eugene.user_service.model.Role;
import com.eugene.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class UserControllerTest
{
    private final String keycloakId = "mock-keycloak-id";
    private final String username = "eugene";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtConverter jwtConverter;
    @MockitoBean
    private UserService userService;
    private UserInfosDto userInfosDto;
    private UserDto userDto;
    private Jwt jwt;
    
    static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @BeforeEach
    void setUp() {
        this.userInfosDto = new UserInfosDto(keycloakId,
                                             username,
                                             List.of(Role.USER.name(),
                                                     Role.ADMIN.name()),
                                             Set.of(1L,
                                                    2L));
        
        this.userDto = new UserDto(username,
                                   Set.of(Role.USER.name(),
                                          Role.ADMIN.name()));
        
        this.jwt = Jwt
                .withTokenValue("token")
                .header("alg",
                        "none")
                .claim("sub",
                       keycloakId)
                .claim("roles",
                       Set.of("USER",
                              "ADMIN"))
                .build();
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void createProfile_withValidData_shouldSucceed() throws Exception {
        given(this.userService.createUser(any(UserDto.class),
                                          eq(keycloakId))).willReturn(userInfosDto);
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(userDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/user/" + keycloakId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.keycloakId").value(keycloakId));
        
        verify(this.userService).createUser(any(UserDto.class),
                                            eq(keycloakId));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void createProfile_withBlankUsername_shouldReturnBadRequest() throws Exception {
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
        
        verify(this.userService,
               never()).createUser(any(UserDto.class),
                                   anyString());
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void createProfile_withNullUsername_shouldReturnBadRequest() throws Exception {
        UserDto invalidUserDto = new UserDto(null,
                                             Set.of("USER"));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidUserDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
        
        verify(this.userService,
               never()).createUser(any(UserDto.class),
                                   anyString());
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void createProfile_withDuplicateUsername_shouldReturnConflict() throws Exception {
        given(this.jwtConverter.extractKeycloakRoles(any(Jwt.class))).willReturn(Set.of("USER",
                                                                                        "ADMIN"));
        given(this.userService.createUser(any(UserDto.class),
                                          eq(keycloakId))).willThrow(new DuplicatedException(
                "User with username '" + username + "' already exists.",
                null));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(userDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isConflict());
        
        verify(this.userService).createUser(any(UserDto.class),
                                            eq(keycloakId));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void createProfile_withDuplicateKeycloakId_shouldReturnConflict() throws Exception {
        given(this.jwtConverter.extractKeycloakRoles(any(Jwt.class))).willReturn(Set.of("USER",
                                                                                        "ADMIN"));
        given(this.userService.createUser(any(UserDto.class),
                                          eq(keycloakId))).willThrow(new DuplicatedException(
                "User with Keycloak ID '" + keycloakId + "' already exists.",
                null));
        
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(userDto))
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt)
                                               .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                                 .with(csrf()))
                .andExpect(status().isConflict());
        
        verify(this.userService).createUser(any(UserDto.class),
                                            eq(keycloakId));
    }
    
    @Test
    void createProfile_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(post("/api/user/create/profile")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(userDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.userService,
               never()).createUser(any(UserDto.class),
                                   anyString());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_withAdminRole_shouldSucceed() throws Exception {
        List<UserInfosDto> users = List.of(userInfosDto);
        given(this.userService.getAllUsers()).willReturn(users);
        
        this.mockMvc
                .perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value(username));
        
        verify(this.userService).getAllUsers();
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void getAllUsers_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(get("/api/user/all"))
                .andExpect(status().isForbidden());
        
        verify(this.userService,
               never()).getAllUsers();
    }
    
    @Test
    void getAllUsers_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/user/all"))
                .andExpect(status().isUnauthorized());
        
        verify(this.userService,
               never()).getAllUsers();
    }
    
    @Test
    @WithMockUser
    void getCurrentUser_withAuthenticatedUser_shouldSucceed() throws Exception {
        given(this.userService.getUserById(keycloakId)).willReturn(userInfosDto);
        
        this.mockMvc
                .perform(get("/api/user/me").with(SecurityMockMvcRequestPostProcessors
                                                          .jwt()
                                                          .jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.keycloakId").value(keycloakId));
        
        verify(this.userService).getUserById(keycloakId);
    }
    
    @Test
    @WithMockUser
    void getCurrentUser_withNonExistingUser_shouldReturnNotFound() throws Exception {
        given(this.userService.getUserById(keycloakId)).willThrow(new NotFoundException("User not found",
                                                                                        null));
        
        this.mockMvc
                .perform(get("/api/user/me").with(SecurityMockMvcRequestPostProcessors
                                                          .jwt()
                                                          .jwt(jwt)))
                .andExpect(status().isNotFound());
        
        verify(this.userService).getUserById(keycloakId);
    }
    
    @Test
    void getCurrentUser_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
        
        verify(this.userService,
               never()).getUserById(anyString());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_withAdminRole_shouldSucceed() throws Exception {
        given(this.userService.getUserById(keycloakId)).willReturn(userInfosDto);
        
        this.mockMvc
                .perform(get("/api/user/{keycloakId}",
                             keycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.keycloakId").value(keycloakId));
        
        verify(this.userService).getUserById(keycloakId);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_withNonExistingId_shouldReturnNotFound() throws Exception {
        given(this.userService.getUserById(keycloakId)).willThrow(new NotFoundException("User not found",
                                                                                        null));
        
        this.mockMvc
                .perform(get("/api/user/{keycloakId}",
                             keycloakId))
                .andExpect(status().isNotFound());
        
        verify(this.userService).getUserById(keycloakId);
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void getUserById_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(get("/api/user/{keycloakId}",
                             keycloakId))
                .andExpect(status().isForbidden());
        
        verify(this.userService,
               never()).getUserById(anyString());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserByUsername_withAdminRole_shouldSucceed() throws Exception {
        given(this.userService.getUserByUsername(username)).willReturn(userInfosDto);
        
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.keycloakId").value(keycloakId));
        
        verify(this.userService).getUserByUsername(username);
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserByUsername_withNonExistingUsername_shouldReturnNotFound() throws Exception {
        given(this.userService.getUserByUsername(username)).willThrow(new NotFoundException("User not found",
                                                                                            null));
        
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             username))
                .andExpect(status().isNotFound());
        
        verify(this.userService).getUserByUsername(username);
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void getUserByUsername_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(get("/api/user/username/{username}",
                             username))
                .andExpect(status().isForbidden());
        
        verify(this.userService,
               never()).getUserByUsername(anyString());
    }
    
    @Test
    @WithMockUser
    void isUserExist_withExistingUser_shouldReturnTrue() throws Exception {
        given(this.userService.isUserExist(keycloakId)).willReturn(true);
        
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakId}",
                             keycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
        
        verify(this.userService).isUserExist(keycloakId);
    }
    
    @Test
    @WithMockUser
    void isUserExist_withNonExistingUser_shouldReturnFalse() throws Exception {
        given(this.userService.isUserExist(keycloakId)).willReturn(false);
        
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakId}",
                             keycloakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
        
        verify(this.userService).isUserExist(keycloakId);
    }
    
    @Test
    void isUserExist_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/user/exists/{keycloakId}",
                             keycloakId))
                .andExpect(status().isUnauthorized());
        
        verify(this.userService,
               never()).isUserExist(anyString());
    }
    
    @Test
    @WithMockUser
    void deleteCurrentUser_withAuthenticatedUser_shouldSucceed() throws Exception {
        doNothing()
                .when(this.userService)
                .deleteUser(keycloakId);
        
        this.mockMvc
                .perform(delete("/api/user/delete/me")
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt))
                                 .with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(this.userService).deleteUser(keycloakId);
    }
    
    @Test
    @WithMockUser
    void deleteCurrentUser_withNonExistingUser_shouldReturnNotFound() throws Exception {
        doThrow(new NotFoundException("User not found",
                                      null))
                .when(this.userService)
                .deleteUser(keycloakId);
        
        this.mockMvc
                .perform(delete("/api/user/delete/me")
                                 .with(SecurityMockMvcRequestPostProcessors
                                               .jwt()
                                               .jwt(jwt))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.userService).deleteUser(keycloakId);
    }
    
    @Test
    void deleteCurrentUser_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(delete("/api/user/delete/me").with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.userService,
               never()).deleteUser(anyString());
    }
}