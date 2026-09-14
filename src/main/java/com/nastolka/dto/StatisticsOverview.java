package com.nastolka.dto;

public class StatisticsOverview {

    private long totalFinishedSessions;
    private long totalPlayTimeMinutes;
    private Double averageSessionLengthMinutes;
    private Double averageRating;

    public StatisticsOverview() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getTotalFinishedSessions() {
        return totalFinishedSessions;
    }

    public void setTotalFinishedSessions(long totalFinishedSessions) {
        this.totalFinishedSessions = totalFinishedSessions;
    }

    public long getTotalPlayTimeMinutes() {
        return totalPlayTimeMinutes;
    }

    public void setTotalPlayTimeMinutes(long totalPlayTimeMinutes) {
        this.totalPlayTimeMinutes = totalPlayTimeMinutes;
    }

    public Double getAverageSessionLengthMinutes() {
        return averageSessionLengthMinutes;
    }

    public void setAverageSessionLengthMinutes(Double averageSessionLengthMinutes) {
        this.averageSessionLengthMinutes = averageSessionLengthMinutes;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public static class Builder {
        private long totalFinishedSessions;
        private long totalPlayTimeMinutes;
        private Double averageSessionLengthMinutes;
        private Double averageRating;

        public Builder totalFinishedSessions(long totalFinishedSessions) {
            this.totalFinishedSessions = totalFinishedSessions;
            return this;
        }

        public Builder totalPlayTimeMinutes(long totalPlayTimeMinutes) {
            this.totalPlayTimeMinutes = totalPlayTimeMinutes;
            return this;
        }

        public Builder averageSessionLengthMinutes(Double averageSessionLengthMinutes) {
            this.averageSessionLengthMinutes = averageSessionLengthMinutes;
            return this;
        }

        public Builder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public StatisticsOverview build() {
            StatisticsOverview overview = new StatisticsOverview();
            overview.totalFinishedSessions = totalFinishedSessions;
            overview.totalPlayTimeMinutes = totalPlayTimeMinutes;
            overview.averageSessionLengthMinutes = averageSessionLengthMinutes;
            overview.averageRating = averageRating;
            return overview;
        }
    }
}
