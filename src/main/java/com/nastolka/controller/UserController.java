package com.nastolka.controller;

import com.nastolka.dto.UpdateUserProfileRequest;
import com.nastolka.dto.UserProfileResponse;
import com.nastolka.dto.UserSearchResult;
import com.nastolka.entity.User;
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

    public UserController(UserService userService) {
        this.userService = userService;
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
                .map(user -> new UserSearchResult(user.getUsername(), user.getDisplayName()))
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
    public ResponseEntity<UserProfileResponse> updateCurrentUser(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setDisplayName(request.getDisplayName());
        User saved = userService.save(user);

        return ResponseEntity.ok(toProfileResponse(saved));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(user.getUsername(), user.getEmail(), user.getDisplayName());
    }
}
