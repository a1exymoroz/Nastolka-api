package com.nastolka.dto;

import java.util.List;

public class LocationStatisticsResponse {

    private Long locationId;
    private StatisticsOverview overview;
    private GameStatistics gameStats;
    private List<PlayerStatisticResponse> playerLeaderboard;
    private List<PlayerStatisticResponse> mostActivePlayers;
    private ActivityStatistics activity;
    private List<ExpansionUsageResponse> mostUsedExpansions;
    private List<DailyActivityResponse> contributionCalendar;

    public LocationStatisticsResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public StatisticsOverview getOverview() {
        return overview;
    }

    public void setOverview(StatisticsOverview overview) {
        this.overview = overview;
    }

    public GameStatistics getGameStats() {
        return gameStats;
    }

    public void setGameStats(GameStatistics gameStats) {
        this.gameStats = gameStats;
    }

    public List<PlayerStatisticResponse> getPlayerLeaderboard() {
        return playerLeaderboard;
    }

    public void setPlayerLeaderboard(List<PlayerStatisticResponse> playerLeaderboard) {
        this.playerLeaderboard = playerLeaderboard;
    }

    public List<PlayerStatisticResponse> getMostActivePlayers() {
        return mostActivePlayers;
    }

    public void setMostActivePlayers(List<PlayerStatisticResponse> mostActivePlayers) {
        this.mostActivePlayers = mostActivePlayers;
    }

    public ActivityStatistics getActivity() {
        return activity;
    }

    public void setActivity(ActivityStatistics activity) {
        this.activity = activity;
    }

    public List<ExpansionUsageResponse> getMostUsedExpansions() {
        return mostUsedExpansions;
    }

    public void setMostUsedExpansions(List<ExpansionUsageResponse> mostUsedExpansions) {
        this.mostUsedExpansions = mostUsedExpansions;
    }

    public List<DailyActivityResponse> getContributionCalendar() {
        return contributionCalendar;
    }

    public void setContributionCalendar(List<DailyActivityResponse> contributionCalendar) {
        this.contributionCalendar = contributionCalendar;
    }

    public static class Builder {
        private Long locationId;
        private StatisticsOverview overview;
        private GameStatistics gameStats;
        private List<PlayerStatisticResponse> playerLeaderboard;
        private List<PlayerStatisticResponse> mostActivePlayers;
        private ActivityStatistics activity;
        private List<ExpansionUsageResponse> mostUsedExpansions;
        private List<DailyActivityResponse> contributionCalendar;

        public Builder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public Builder overview(StatisticsOverview overview) {
            this.overview = overview;
            return this;
        }

        public Builder gameStats(GameStatistics gameStats) {
            this.gameStats = gameStats;
            return this;
        }

        public Builder playerLeaderboard(List<PlayerStatisticResponse> playerLeaderboard) {
            this.playerLeaderboard = playerLeaderboard;
            return this;
        }

        public Builder mostActivePlayers(List<PlayerStatisticResponse> mostActivePlayers) {
            this.mostActivePlayers = mostActivePlayers;
            return this;
        }

        public Builder activity(ActivityStatistics activity) {
            this.activity = activity;
            return this;
        }

        public Builder mostUsedExpansions(List<ExpansionUsageResponse> mostUsedExpansions) {
            this.mostUsedExpansions = mostUsedExpansions;
            return this;
        }

        public Builder contributionCalendar(List<DailyActivityResponse> contributionCalendar) {
            this.contributionCalendar = contributionCalendar;
            return this;
        }

        public LocationStatisticsResponse build() {
            LocationStatisticsResponse response = new LocationStatisticsResponse();
            response.locationId = locationId;
            response.overview = overview;
            response.gameStats = gameStats;
            response.playerLeaderboard = playerLeaderboard;
            response.mostActivePlayers = mostActivePlayers;
            response.activity = activity;
            response.mostUsedExpansions = mostUsedExpansions;
            response.contributionCalendar = contributionCalendar;
            return response;
        }
    }
}
