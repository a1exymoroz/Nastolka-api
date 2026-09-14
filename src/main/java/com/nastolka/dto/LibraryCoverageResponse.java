package com.nastolka.dto;

public class LibraryCoverageResponse {

    private long gamesPlayed;
    private long totalGamesInLibrary;
    private Double coveragePercentage;

    public LibraryCoverageResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(long gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public long getTotalGamesInLibrary() {
        return totalGamesInLibrary;
    }

    public void setTotalGamesInLibrary(long totalGamesInLibrary) {
        this.totalGamesInLibrary = totalGamesInLibrary;
    }

    public Double getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(Double coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    public static class Builder {
        private long gamesPlayed;
        private long totalGamesInLibrary;
        private Double coveragePercentage;

        public Builder gamesPlayed(long gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
            return this;
        }

        public Builder totalGamesInLibrary(long totalGamesInLibrary) {
            this.totalGamesInLibrary = totalGamesInLibrary;
            return this;
        }

        public Builder coveragePercentage(Double coveragePercentage) {
            this.coveragePercentage = coveragePercentage;
            return this;
        }

        public LibraryCoverageResponse build() {
            LibraryCoverageResponse response = new LibraryCoverageResponse();
            response.gamesPlayed = gamesPlayed;
            response.totalGamesInLibrary = totalGamesInLibrary;
            response.coveragePercentage = coveragePercentage;
            return response;
        }
    }
}
