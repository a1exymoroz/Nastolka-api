package com.nastolka.dto;

public class PlayerResultResponse {

    private String username;
    private Integer placement;
    private Integer points;

    public PlayerResultResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getPlacement() {
        return placement;
    }

    public void setPlacement(Integer placement) {
        this.placement = placement;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public static class Builder {
        private String username;
        private Integer placement;
        private Integer points;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder placement(Integer placement) {
            this.placement = placement;
            return this;
        }

        public Builder points(Integer points) {
            this.points = points;
            return this;
        }

        public PlayerResultResponse build() {
            PlayerResultResponse response = new PlayerResultResponse();
            response.username = username;
            response.placement = placement;
            response.points = points;
            return response;
        }
    }
}
