package com.nastolka.dto;

import java.time.LocalDate;

public class DailyActivityResponse {

    private LocalDate date;
    private long sessionCount;

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

    public static class Builder {
        private LocalDate date;
        private long sessionCount;

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder sessionCount(long sessionCount) {
            this.sessionCount = sessionCount;
            return this;
        }

        public DailyActivityResponse build() {
            DailyActivityResponse response = new DailyActivityResponse();
            response.date = date;
            response.sessionCount = sessionCount;
            return response;
        }
    }
}
