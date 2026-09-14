package com.nastolka.dto;

import java.util.List;

public class PlayerStatisticsResponse {

    private List<PlayerStatisticResponse> leaderboard;
    private List<PlayerStatisticResponse> mostActive;

    public PlayerStatisticsResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<PlayerStatisticResponse> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<PlayerStatisticResponse> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public List<PlayerStatisticResponse> getMostActive() {
        return mostActive;
    }

    public void setMostActive(List<PlayerStatisticResponse> mostActive) {
        this.mostActive = mostActive;
    }

    public static class Builder {
        private List<PlayerStatisticResponse> leaderboard;
        private List<PlayerStatisticResponse> mostActive;

        public Builder leaderboard(List<PlayerStatisticResponse> leaderboard) {
            this.leaderboard = leaderboard;
            return this;
        }

        public Builder mostActive(List<PlayerStatisticResponse> mostActive) {
            this.mostActive = mostActive;
            return this;
        }

        public PlayerStatisticsResponse build() {
            PlayerStatisticsResponse response = new PlayerStatisticsResponse();
            response.leaderboard = leaderboard;
            response.mostActive = mostActive;
            return response;
        }
    }
}
