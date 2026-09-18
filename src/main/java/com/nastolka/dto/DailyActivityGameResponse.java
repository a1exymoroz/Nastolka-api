package com.nastolka.dto;

public class DailyActivityGameResponse {

    private Long id;
    private String name;

    public DailyActivityGameResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Builder {
        private Long id;
        private String name;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public DailyActivityGameResponse build() {
            DailyActivityGameResponse response = new DailyActivityGameResponse();
            response.id = id;
            response.name = name;
            return response;
        }
    }
}
