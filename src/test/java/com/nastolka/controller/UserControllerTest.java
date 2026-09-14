package com.nastolka.controller;

import com.nastolka.entity.User;
import com.nastolka.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor asUser(String username) {
        return request -> {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            return request;
        };
    }

    @Test
    void getCurrentUser_returnsProfile() throws Exception {
        User user = User.builder().username("alice").email("alice@example.com").displayName("Alice").build();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me").with(asUser("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.displayName").value("Alice"));
    }

    @Test
    void getCurrentUser_returnsNotFoundWhenUserMissing() throws Exception {
        when(userService.findByUsername("alice")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me").with(asUser("alice")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCurrentUser_updatesDisplayName() throws Exception {
        User user = User.builder().username("alice").email("alice@example.com").build();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Alice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Alice"));

        verify(userService).save(user);
    }

    @Test
    void updateCurrentUser_clearsDisplayNameWhenNull() throws Exception {
        User user = User.builder().username("alice").email("alice@example.com").displayName("Alice").build();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").doesNotExist());
    }

    @Test
    void updateCurrentUser_rejectsBlankDisplayName() throws Exception {
        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUser_rejectsDisplayNameOverFiftyCharacters() throws Exception {
        String tooLong = "a".repeat(51);

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUser_returnsNotFoundWhenUserMissing() throws Exception {
        when(userService.findByUsername("alice")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Alice\"}"))
                .andExpect(status().isNotFound());
    }
}
