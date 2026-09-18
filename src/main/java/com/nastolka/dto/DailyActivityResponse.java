package com.nastolka.dto;

import java.time.LocalDate;
import java.util.List;

public class DailyActivityResponse {

    private LocalDate date;
    private long sessionCount;
    private List<DailyActivityGameResponse> games;

    public DailyActivityResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public List<DailyActivityGameResponse> getGames() {
        return games;
    }

    public void setGames(List<DailyActivityGameResponse> games) {
        this.games = games;
    }

    public static class Builder {
        private LocalDate date;
        private long sessionCount;
        private List<DailyActivityGameResponse> games;

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder sessionCount(long sessionCount) {
            this.sessionCount = sessionCount;
            return this;
        }

        public Builder games(List<DailyActivityGameResponse> games) {
            this.games = games;
            return this;
        }

        public DailyActivityResponse build() {
            DailyActivityResponse response = new DailyActivityResponse();
            response.date = date;
            response.sessionCount = sessionCount;
            response.games = games;
            return response;
        }
    }
}
