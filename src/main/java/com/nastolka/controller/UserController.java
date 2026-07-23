package com.nastolka.controller;

import com.nastolka.dto.UserSearchResult;
import com.nastolka.entity.User;
import com.nastolka.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<List<UserSearchResult>> search(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank");
        }

        List<UserSearchResult> results = userService.search(query.trim()).stream()
                .map(User::getUsername)
                .map(UserSearchResult::new)
                .toList();

        return ResponseEntity.ok(results);
    }
}
