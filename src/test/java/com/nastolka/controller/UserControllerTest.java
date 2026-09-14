package com.nastolka.controller;

import com.nastolka.entity.User;
import com.nastolka.security.JwtUtil;
import com.nastolka.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.hamcrest.Matchers;
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
import static org.mockito.Mockito.never;
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

    private JwtUtil jwtUtil;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-at-least-32-bytes-long!!", 3600000L);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, jwtUtil))
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
        User user = User.builder().username("alice").email("alice@example.com").build();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me").with(asUser("alice")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getCurrentUser_returnsNotFoundWhenUserMissing() throws Exception {
        when(userService.findByUsername("alice")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me").with(asUser("alice")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCurrentUser_changesUsernameAndReturnsFreshToken() throws Exception {
        User user = User.builder().username("alice").email("alice@example.com").build();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userService.existsByUsername("alice2")).thenReturn(false);
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice2"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.token").value(Matchers.not(Matchers.emptyOrNullString())));

        verify(userService).save(user);
    }

    @Test
    void updateCurrentUser_allowsResubmittingSameUsername() throws Exception {
        User user = User.builder().username("alice").email("alice@example.com").build();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userService.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        verify(userService, never()).existsByUsername(any());
    }

    @Test
    void updateCurrentUser_rejectsUsernameAlreadyTaken() throws Exception {
        User user = User.builder().username("alice").email("alice@example.com").build();
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userService.existsByUsername("bob")).thenReturn(true);

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\"}"))
                .andExpect(status().isConflict());

        verify(userService, never()).save(any());
    }

    @Test
    void updateCurrentUser_rejectsBlankUsername() throws Exception {
        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUser_rejectsUsernameOverFiftyCharacters() throws Exception {
        String tooLong = "a".repeat(51);

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrentUser_returnsNotFoundWhenUserMissing() throws Exception {
        when(userService.findByUsername("alice")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/me").with(asUser("alice"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice2\"}"))
                .andExpect(status().isNotFound());
    }
}
