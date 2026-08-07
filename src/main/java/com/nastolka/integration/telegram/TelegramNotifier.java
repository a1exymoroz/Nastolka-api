package com.nastolka.integration.telegram;

import com.nastolka.dto.HistoryResponse;
import com.nastolka.dto.PlayerResultResponse;
import com.nastolka.entity.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TelegramNotifier {

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    private final RestClient restClient;
    private final String token;

    public TelegramNotifier(RestClient telegramRestClient, @Value("${app.telegram.bot-token:}") String token) {
        this.restClient = telegramRestClient;
        this.token = token;
    }

    public void notifyHistoryAdded(Location location, HistoryResponse history) {
        String chatId = location.getTelegramChatId();
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            return;
        }

        String text = buildMessage(location, history);
        try {
            restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("Failed to send Telegram notification for location {}", location.getId(), e);
        }
    }

    private String buildMessage(Location location, HistoryResponse history) {
        StringBuilder text = new StringBuilder();
        text.append("🎲 ").append(location.getName()).append('\n');
        text.append(history.getGameName()).append(" logged");

        List<PlayerResultResponse> players = history.getPlayers();
        if (players != null && !players.isEmpty()) {
            text.append('\n').append("Players: ").append(players.stream()
                    .map(this::formatPlayer)
                    .collect(Collectors.joining(", ")));
        }

        return text.toString();
    }

    private String formatPlayer(PlayerResultResponse player) {
        return player.getPlacement() != null
                ? player.getUsername() + " (#" + player.getPlacement() + ")"
                : player.getUsername();
    }
}
