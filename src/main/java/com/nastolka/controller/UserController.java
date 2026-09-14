package com.nastolka.controller;

import com.nastolka.dto.UpdateUsernameRequest;
import com.nastolka.dto.UserProfileResponse;
import com.nastolka.dto.UsernameUpdateResponse;
import com.nastolka.dto.UserSearchResult;
import com.nastolka.entity.User;
import com.nastolka.security.JwtUtil;
import com.nastolka.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResult>> search(
            @RequestParam String query,
            @AuthenticationPrincipal String username
    ) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank");
        }

        List<UserSearchResult> results = userService.search(query.trim(), username).stream()
                .map(User::getUsername)
                .map(UserSearchResult::new)
                .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ResponseEntity.ok(toProfileResponse(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UsernameUpdateResponse> updateCurrentUser(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody UpdateUsernameRequest request
    ) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String newUsername = request.getUsername();
        if (!newUsername.equals(user.getUsername()) && userService.existsByUsername(newUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        user.setUsername(newUsername);
        User saved = userService.save(user);

        String token = jwtUtil.generateToken(saved.getUsername());

        return ResponseEntity.ok(new UsernameUpdateResponse(saved.getUsername(), saved.getEmail(), token));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(user.getUsername(), user.getEmail());
    }
}
