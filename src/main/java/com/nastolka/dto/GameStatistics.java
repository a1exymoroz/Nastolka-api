package com.nastolka.dto;

import java.util.List;

public class GameStatistics {

    private List<GamePlayCountResponse> mostPlayedGames;
    private List<GameRatingResponse> topRatedGames;
    private LibraryCoverageResponse libraryCoverage;

    public GameStatistics() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<GamePlayCountResponse> getMostPlayedGames() {
        return mostPlayedGames;
    }

    public void setMostPlayedGames(List<GamePlayCountResponse> mostPlayedGames) {
        this.mostPlayedGames = mostPlayedGames;
    }

    public List<GameRatingResponse> getTopRatedGames() {
        return topRatedGames;
    }

    public void setTopRatedGames(List<GameRatingResponse> topRatedGames) {
        this.topRatedGames = topRatedGames;
    }

    public LibraryCoverageResponse getLibraryCoverage() {
        return libraryCoverage;
    }

    public void setLibraryCoverage(LibraryCoverageResponse libraryCoverage) {
        this.libraryCoverage = libraryCoverage;
    }

    public static class Builder {
        private List<GamePlayCountResponse> mostPlayedGames;
        private List<GameRatingResponse> topRatedGames;
        private LibraryCoverageResponse libraryCoverage;

        public Builder mostPlayedGames(List<GamePlayCountResponse> mostPlayedGames) {
            this.mostPlayedGames = mostPlayedGames;
            return this;
        }

        public Builder topRatedGames(List<GameRatingResponse> topRatedGames) {
            this.topRatedGames = topRatedGames;
            return this;
        }

        public Builder libraryCoverage(LibraryCoverageResponse libraryCoverage) {
            this.libraryCoverage = libraryCoverage;
            return this;
        }

        public GameStatistics build() {
            GameStatistics statistics = new GameStatistics();
            statistics.mostPlayedGames = mostPlayedGames;
            statistics.topRatedGames = topRatedGames;
            statistics.libraryCoverage = libraryCoverage;
            return statistics;
        }
    }
}
