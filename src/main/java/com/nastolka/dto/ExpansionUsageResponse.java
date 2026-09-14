package com.nastolka.dto;

public class ExpansionUsageResponse {

    private Long expansionId;
    private String expansionName;
    private long useCount;

    public ExpansionUsageResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getExpansionId() {
        return expansionId;
    }

    public void setExpansionId(Long expansionId) {
        this.expansionId = expansionId;
    }

    public String getExpansionName() {
        return expansionName;
    }

    public void setExpansionName(String expansionName) {
        this.expansionName = expansionName;
    }

    public long getUseCount() {
        return useCount;
    }

    public void setUseCount(long useCount) {
        this.useCount = useCount;
    }

    public static class Builder {
        private Long expansionId;
        private String expansionName;
        private long useCount;

        public Builder expansionId(Long expansionId) {
            this.expansionId = expansionId;
            return this;
        }

        public Builder expansionName(String expansionName) {
            this.expansionName = expansionName;
            return this;
        }

        public Builder useCount(long useCount) {
            this.useCount = useCount;
            return this;
        }

        public ExpansionUsageResponse build() {
            ExpansionUsageResponse response = new ExpansionUsageResponse();
            response.expansionId = expansionId;
            response.expansionName = expansionName;
            response.useCount = useCount;
            return response;
        }
    }
}
