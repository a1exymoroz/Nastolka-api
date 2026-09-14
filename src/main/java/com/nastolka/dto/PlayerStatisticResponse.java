package com.nastolka.dto;

public class PlayerStatisticResponse {

    private String username;
    private long gamesPlayed;
    private long wins;
    private double winRatePercentage;
    private long totalPoints;
    private double averagePoints;

    public PlayerStatisticResponse() {
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

    public long getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(long gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public long getWins() {
        return wins;
    }

    public void setWins(long wins) {
        this.wins = wins;
    }

    public double getWinRatePercentage() {
        return winRatePercentage;
    }

    public void setWinRatePercentage(double winRatePercentage) {
        this.winRatePercentage = winRatePercentage;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public double getAveragePoints() {
        return averagePoints;
    }

    public void setAveragePoints(double averagePoints) {
        this.averagePoints = averagePoints;
    }

    public static class Builder {
        private String username;
        private long gamesPlayed;
        private long wins;
        private double winRatePercentage;
        private long totalPoints;
        private double averagePoints;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder gamesPlayed(long gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
            return this;
        }

        public Builder wins(long wins) {
            this.wins = wins;
            return this;
        }

        public Builder winRatePercentage(double winRatePercentage) {
            this.winRatePercentage = winRatePercentage;
            return this;
        }

        public Builder totalPoints(long totalPoints) {
            this.totalPoints = totalPoints;
            return this;
        }

        public Builder averagePoints(double averagePoints) {
            this.averagePoints = averagePoints;
            return this;
        }

        public PlayerStatisticResponse build() {
            PlayerStatisticResponse response = new PlayerStatisticResponse();
            response.username = username;
            response.gamesPlayed = gamesPlayed;
            response.wins = wins;
            response.winRatePercentage = winRatePercentage;
            response.totalPoints = totalPoints;
            response.averagePoints = averagePoints;
            return response;
        }
    }
}
