package com.nastolka.dto;

public class GameResponse {

    private Long id;
    private String title;
    private String description;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer playTimeMinutes;
    private Boolean available;

    public GameResponse() {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getPlayTimeMinutes() {
        return playTimeMinutes;
    }

    public void setPlayTimeMinutes(Integer playTimeMinutes) {
        this.playTimeMinutes = playTimeMinutes;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private Integer minPlayers;
        private Integer maxPlayers;
        private Integer playTimeMinutes;
        private Boolean available;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder minPlayers(Integer minPlayers) {
            this.minPlayers = minPlayers;
            return this;
        }

        public Builder maxPlayers(Integer maxPlayers) {
            this.maxPlayers = maxPlayers;
            return this;
        }

        public Builder playTimeMinutes(Integer playTimeMinutes) {
            this.playTimeMinutes = playTimeMinutes;
            return this;
        }

        public Builder available(Boolean available) {
            this.available = available;
            return this;
        }

        public GameResponse build() {
            GameResponse response = new GameResponse();
            response.id = id;
            response.title = title;
            response.description = description;
            response.minPlayers = minPlayers;
            response.maxPlayers = maxPlayers;
            response.playTimeMinutes = playTimeMinutes;
            response.available = available;
            return response;
        }
    }
}
