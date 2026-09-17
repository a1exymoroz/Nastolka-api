package com.nastolka.dto;

import com.nastolka.entity.HistoryOutcome;
import com.nastolka.entity.HistoryState;

import java.time.Instant;
import java.util.List;

public class HistoryResponse {

    private Long id;
    private Long locationId;
    private Long gameId;
    private String gameName;
    private Instant playedAt;
    private HistoryState state;
    private Instant startedAt;
    private Instant finishedAt;
    private Long durationMinutes;
    private Integer rating;
    private List<PlayerResultResponse> players;
    private List<ExpansionResponse> expansions;
    private HistoryOutcome outcome;

    public HistoryResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Instant getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Instant playedAt) {
        this.playedAt = playedAt;
    }

    public HistoryState getState() {
        return state;
    }

    public void setState(HistoryState state) {
        this.state = state;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public List<PlayerResultResponse> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerResultResponse> players) {
        this.players = players;
    }

    public List<ExpansionResponse> getExpansions() {
        return expansions;
    }

    public void setExpansions(List<ExpansionResponse> expansions) {
        this.expansions = expansions;
    }

    public HistoryOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(HistoryOutcome outcome) {
        this.outcome = outcome;
    }

    public static class Builder {
        private Long id;
        private Long locationId;
        private Long gameId;
        private String gameName;
        private Instant playedAt;
        private HistoryState state;
        private Instant startedAt;
        private Instant finishedAt;
        private Long durationMinutes;
        private Integer rating;
        private List<PlayerResultResponse> players;
        private List<ExpansionResponse> expansions;
        private HistoryOutcome outcome;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public Builder gameId(Long gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder gameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public Builder playedAt(Instant playedAt) {
            this.playedAt = playedAt;
            return this;
        }

        public Builder state(HistoryState state) {
            this.state = state;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder finishedAt(Instant finishedAt) {
            this.finishedAt = finishedAt;
            return this;
        }

        public Builder durationMinutes(Long durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public Builder players(List<PlayerResultResponse> players) {
            this.players = players;
            return this;
        }

        public Builder expansions(List<ExpansionResponse> expansions) {
            this.expansions = expansions;
            return this;
        }

        public Builder outcome(HistoryOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        public HistoryResponse build() {
            HistoryResponse response = new HistoryResponse();
            response.id = id;
            response.locationId = locationId;
            response.gameId = gameId;
            response.gameName = gameName;
            response.playedAt = playedAt;
            response.state = state;
            response.startedAt = startedAt;
            response.finishedAt = finishedAt;
            response.durationMinutes = durationMinutes;
            response.rating = rating;
            response.players = players;
            response.expansions = expansions;
            response.outcome = outcome;
            return response;
        }
    }
}
