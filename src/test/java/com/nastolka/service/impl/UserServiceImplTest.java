package com.nastolka.service.impl;

import com.nastolka.entity.User;
import com.nastolka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void findByUsername_returnsUserFromRepository() {
        User user = User.builder().id(1L).username("alice").email("alice@example.com").build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("alice");

        assertThat(result).contains(user);
    }

    @Test
    void findByUsername_returnsEmptyWhenNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void save_delegatesToRepository() {
        User user = User.builder().id(1L).username("alice").email("alice@example.com")
                .displayName("Alice").build();
        when(userRepository.save(user)).thenReturn(user);

        User saved = userService.save(user);

        assertThat(saved.getDisplayName()).isEqualTo("Alice");
        verify(userRepository).save(user);
    }
}
