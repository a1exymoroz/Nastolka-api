package com.nastolka.dto;

import java.time.Instant;

public class PickSessionParticipantResponse {

    private Long userId;
    private String username;
    private Integer turnOrder;
    private Instant joinedAt;

    public PickSessionParticipantResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(Integer turnOrder) {
        this.turnOrder = turnOrder;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public static class Builder {
        private Long userId;
        private String username;
        private Integer turnOrder;
        private Instant joinedAt;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder turnOrder(Integer turnOrder) {
            this.turnOrder = turnOrder;
            return this;
        }

        public Builder joinedAt(Instant joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public PickSessionParticipantResponse build() {
            PickSessionParticipantResponse response = new PickSessionParticipantResponse();
            response.userId = userId;
            response.username = username;
            response.turnOrder = turnOrder;
            response.joinedAt = joinedAt;
            return response;
        }
    }
}
