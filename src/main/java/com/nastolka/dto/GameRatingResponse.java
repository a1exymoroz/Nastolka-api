package com.nastolka.dto;

public class GameRatingResponse {

    private Long gameId;
    private String gameName;
    private double averageRating;
    private long ratingCount;

    public GameRatingResponse() {
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

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public static class Builder {
        private Long gameId;
        private String gameName;
        private double averageRating;
        private long ratingCount;

        public Builder gameId(Long gameId) {
            this.gameId = gameId;
            return this;
        }

        public Builder gameName(String gameName) {
            this.gameName = gameName;
            return this;
        }

        public Builder averageRating(double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public Builder ratingCount(long ratingCount) {
            this.ratingCount = ratingCount;
            return this;
        }

        public GameRatingResponse build() {
            GameRatingResponse response = new GameRatingResponse();
            response.gameId = gameId;
            response.gameName = gameName;
            response.averageRating = averageRating;
            response.ratingCount = ratingCount;
            return response;
        }
    }
}
