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
import org.springframework.web.util.HtmlUtils;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class TelegramNotifier {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    private static final Logger log = LoggerFactory.getLogger(TelegramNotifier.class);

    private final RestClient restClient;
    private final String token;
    private final boolean isProd;
    private final String webUrl;

    public TelegramNotifier(
            RestClient telegramRestClient,
            @Value("${app.telegram.bot-token:}") String token,
            @Value("${spring.profiles.active:}") String activeProfile,
            @Value("${app.web.url:}") String webUrl
    ) {
        this.restClient = telegramRestClient;
        this.token = token;
        this.isProd = "prod".equalsIgnoreCase(activeProfile);
        this.webUrl = webUrl;
    }

    public void notifyHistoryFinished(Location location, HistoryResponse history) {
        String chatId = location.getTelegramChatId();
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            log.info("Skipping Telegram notification for location {}: bot token or telegramChatId not configured",
                    location.getId());
            return;
        }

        String text = buildMessage(location, history);
        try {
            restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", chatId, "text", text, "parse_mode", "HTML"))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Sent Telegram notification for history {} at location {}", history.getId(), location.getId());
        } catch (RestClientException e) {
            log.warn("Failed to send Telegram notification for location {}", location.getId(), e);
        }
    }

    private String buildMessage(Location location, HistoryResponse history) {
        StringBuilder text = new StringBuilder();
        if (!isProd) {
            text.append("🧪 <b>[DEV]</b>\n");
        }
        text.append("🎲 <b>").append(HtmlUtils.htmlEscape(history.getGameName())).append("</b>\n");
        text.append("✅ Finished\n");

        List<String> details = new ArrayList<>();
        if (history.getPlayedAt() != null) {
            details.add("📅 " + DATE_FORMATTER.format(history.getPlayedAt()));
        }
        if (history.getDurationMinutes() != null) {
            details.add("⏱ " + formatDuration(history.getDurationMinutes()));
        }
        if (!details.isEmpty()) {
            text.append(String.join("   ", details)).append('\n');
        }

        List<PlayerResultResponse> players = history.getPlayers();
        if (players != null && !players.isEmpty()) {
            text.append('\n');
            for (int i = 0; i < players.size(); i++) {
                PlayerResultResponse player = players.get(i);
                text.append(rankMarker(i)).append(' ').append(HtmlUtils.htmlEscape(player.getUsername()));
                if (player.getPoints() != null) {
                    text.append(" — <b>").append(player.getPoints()).append(" pts</b>");
                }
                text.append('\n');
            }
        }

        String baseUrl = resolveBaseUrl();
        if (baseUrl != null) {
            text.append('\n');
            text.append("🔗 <a href=\"").append(baseUrl)
                    .append("/locations/").append(location.getId())
                    .append("/history/").append(history.getId())
                    .append("\">View details</a>");
        }

        return text.toString().stripTrailing();
    }

    private String rankMarker(int index) {
        return switch (index) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> (index + 1) + ".";
        };
    }

    private String resolveBaseUrl() {
        if (webUrl == null || webUrl.isBlank()) {
            return null;
        }
        String trimmed = webUrl.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    private String formatDuration(long minutes) {
        long hours = minutes / 60;
        long remainder = minutes % 60;
        return hours == 0 ? remainder + "m" : hours + "h " + remainder + "m";
    }
}
