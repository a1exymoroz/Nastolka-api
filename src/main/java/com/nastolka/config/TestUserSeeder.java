package com.nastolka.config;

import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a handful of simple login/password test users (user1..user5) for local development only.
 */
@Component
@Profile("local")
@Order(1)
public class TestUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TestUserSeeder.class);
    private static final String TEST_PASSWORD = "Pass1234@";
    private static final int TEST_USER_COUNT = 5;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public TestUserSeeder(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        for (int i = 1; i <= TEST_USER_COUNT; i++) {
            String username = "user" + i;
            if (userService.existsByUsername(username)) {
                continue;
            }
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(TEST_PASSWORD))
                    .email(username + "@nastolka.local")
                    .role(Role.USER)
                    .build();
            userService.save(user);
            log.info("Created test user '{}'", username);
        }
    }
}
