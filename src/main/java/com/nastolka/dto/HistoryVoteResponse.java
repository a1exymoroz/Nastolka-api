package com.nastolka.dto;

import java.time.Instant;

public class HistoryVoteResponse {

    private String username;
    private Integer score;
    private Instant votedAt;

    public HistoryVoteResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Instant getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(Instant votedAt) {
        this.votedAt = votedAt;
    }

    public static class Builder {
        private String username;
        private Integer score;
        private Instant votedAt;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder score(Integer score) {
            this.score = score;
            return this;
        }

        public Builder votedAt(Instant votedAt) {
            this.votedAt = votedAt;
            return this;
        }

        public HistoryVoteResponse build() {
            HistoryVoteResponse response = new HistoryVoteResponse();
            response.username = username;
            response.score = score;
            response.votedAt = votedAt;
            return response;
        }
    }
}
