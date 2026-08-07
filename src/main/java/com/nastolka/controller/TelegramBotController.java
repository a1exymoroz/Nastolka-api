package com.nastolka.controller;

import com.nastolka.dto.HistoryResponse;
import com.nastolka.service.LocationHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/api/telegram")
public class TelegramBotController {

    private static final int RECENT_HISTORY_LIMIT = 5;

    private final LocationHistoryService locationHistoryService;
    private final String botSecret;

    public TelegramBotController(
            LocationHistoryService locationHistoryService,
            @Value("${app.telegram.bot-secret:}") String botSecret
    ) {
        this.locationHistoryService = locationHistoryService;
        this.botSecret = botSecret;
    }

    @GetMapping("/history")
    public ResponseEntity<List<HistoryResponse>> getRecentHistory(
            @RequestParam String chatId,
            @RequestHeader(value = "X-Telegram-Bot-Secret", required = false) String providedSecret
    ) {
        requireValidSecret(providedSecret);
        return ResponseEntity.ok(locationHistoryService.getRecentHistoryByChatId(chatId, RECENT_HISTORY_LIMIT));
    }

    private void requireValidSecret(String providedSecret) {
        boolean valid = !botSecret.isBlank() && providedSecret != null
                && MessageDigest.isEqual(
                        botSecret.getBytes(StandardCharsets.UTF_8),
                        providedSecret.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
