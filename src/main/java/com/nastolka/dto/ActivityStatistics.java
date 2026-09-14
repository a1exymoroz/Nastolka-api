package com.nastolka.dto;

import java.util.List;

public class ActivityStatistics {

    private ActivityGranularity granularity;
    private List<ActivityBucketResponse> buckets;

    public ActivityStatistics() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public ActivityGranularity getGranularity() {
        return granularity;
    }

    public void setGranularity(ActivityGranularity granularity) {
        this.granularity = granularity;
    }

    public List<ActivityBucketResponse> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<ActivityBucketResponse> buckets) {
        this.buckets = buckets;
    }

    public static class Builder {
        private ActivityGranularity granularity;
        private List<ActivityBucketResponse> buckets;

        public Builder granularity(ActivityGranularity granularity) {
            this.granularity = granularity;
            return this;
        }

        public Builder buckets(List<ActivityBucketResponse> buckets) {
            this.buckets = buckets;
            return this;
        }

        public ActivityStatistics build() {
            ActivityStatistics statistics = new ActivityStatistics();
            statistics.granularity = granularity;
            statistics.buckets = buckets;
            return statistics;
        }
    }
}
