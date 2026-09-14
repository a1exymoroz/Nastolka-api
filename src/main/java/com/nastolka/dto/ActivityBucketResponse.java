package com.nastolka.dto;

import java.time.LocalDate;

public class ActivityBucketResponse {

    private LocalDate bucketStart;
    private long sessionCount;

    public ActivityBucketResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDate getBucketStart() {
        return bucketStart;
    }

    public void setBucketStart(LocalDate bucketStart) {
        this.bucketStart = bucketStart;
    }

    public long getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public static class Builder {
        private LocalDate bucketStart;
        private long sessionCount;

        public Builder bucketStart(LocalDate bucketStart) {
            this.bucketStart = bucketStart;
            return this;
        }

        public Builder sessionCount(long sessionCount) {
            this.sessionCount = sessionCount;
            return this;
        }

        public ActivityBucketResponse build() {
            ActivityBucketResponse response = new ActivityBucketResponse();
            response.bucketStart = bucketStart;
            response.sessionCount = sessionCount;
            return response;
        }
    }
}
