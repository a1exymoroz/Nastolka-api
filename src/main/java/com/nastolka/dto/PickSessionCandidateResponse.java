package com.nastolka.dto;

import com.nastolka.entity.PickSessionCandidateAction;

import java.time.Instant;

public class PickSessionCandidateResponse {

    private Long gameId;
    private String gameName;
    private PickSessionCandidateAction action;
    private String actedByUsername;
    private Instant actedAt;

    public PickSessionCandidateResponse() {
    }

    public static Builder builder() {
        return new Builder();
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

    public PickSessionCandidateAction getAction() {
        return action;
    }

    public void setAction(PickSessionCandidateAction action) {
        this.action = action;
    }

    public String getActedByUsername() {
        return actedByUsername;
    }

    public void setActedByUsername(String actedByUsername) {
        this.actedByUsername = actedByUsername;
    }

    public Instant getActedAt() {
        return actedAt;
    }

    public void setActedAt(Instant actedAt) {
        this.actedAt = actedAt;
    }

    public static class Builder {
        private Long gameId;
        private String gameName;
        private PickSessionCandidateAction action;
        private String actedByUsername;
        private Instant actedAt;

        public Builder gameId(Long gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder gameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public Builder action(PickSessionCandidateAction action) {
            this.action = action;
            return this;
        }

        public Builder actedByUsername(String actedByUsername) {
            this.actedByUsername = actedByUsername;
            return this;
        }

        public Builder actedAt(Instant actedAt) {
            this.actedAt = actedAt;
            return this;
        }

        public PickSessionCandidateResponse build() {
            PickSessionCandidateResponse response = new PickSessionCandidateResponse();
            response.gameId = gameId;
            response.gameName = gameName;
            response.action = action;
            response.actedByUsername = actedByUsername;
            response.actedAt = actedAt;
            return response;
        }
    }
}
