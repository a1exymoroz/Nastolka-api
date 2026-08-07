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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class TelegramNotifier {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.ENGLISH);

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    private final RestClient restClient;
    private final String token;
    private final boolean isProd;

    public TelegramNotifier(
            RestClient telegramRestClient,
            @Value("${app.telegram.bot-token:}") String token,
            @Value("${spring.profiles.active:}") String activeProfile
    ) {
        this.restClient = telegramRestClient;
        this.token = token;
        this.isProd = "prod".equalsIgnoreCase(activeProfile);
    }

    public void notifyHistoryFinished(Location location, HistoryResponse history) {
        String chatId = location.getTelegramChatId();
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            log.info("Skipping Telegram notification for location {}: bot token or telegramChatId not configured",
                    location.getId());
            return;
        }

        String text = buildMessage(history);
        try {
            restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "text", text))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Sent Telegram notification for history {} at location {}", history.getId(), location.getId());
        } catch (RestClientException e) {
            log.warn("Failed to send Telegram notification for location {}", location.getId(), e);
        }
    }

    private String buildMessage(HistoryResponse history) {
        StringBuilder text = new StringBuilder();
        if (!isProd) {
            text.append("🧪 [DEV] ");
        }
        text.append("🎲 ").append(history.getGameName()).append('\n');
        text.append("✅ Finished\n");
        if (history.getPlayedAt() != null) {
            text.append(DATE_FORMATTER.format(history.getPlayedAt())).append('\n');
        }
        if (history.getDurationMinutes() != null) {
            text.append("Duration: ").append(formatDuration(history.getDurationMinutes())).append('\n');
        }

        List<PlayerResultResponse> players = history.getPlayers();
        if (players != null && !players.isEmpty()) {
            text.append('\n');
            for (int i = 0; i < players.size(); i++) {
                PlayerResultResponse player = players.get(i);
                text.append(i + 1).append(". ").append(player.getUsername());
                if (player.getPoints() != null) {
                    text.append(" (").append(player.getPoints()).append(" pts)");
                }
                text.append('\n');
            }
        }

        return text.toString().stripTrailing();
    }

    private String formatDuration(long minutes) {
        long hours = minutes / 60;
        long remainder = minutes % 60;
        return hours == 0 ? remainder + "m" : hours + "h " + remainder + "m";
    }
}
