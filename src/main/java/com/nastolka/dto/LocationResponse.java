package com.nastolka.dto;

public class LocationResponse {

    private Long id;
    private String name;
    private String description;
    private String ownerUsername;
    private String telegramChatId;

    public LocationResponse() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private String ownerUsername;
        private String telegramChatId;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder ownerUsername(String ownerUsername) {
            this.ownerUsername = ownerUsername;
            return this;
        }

        public Builder telegramChatId(String telegramChatId) {
            this.telegramChatId = telegramChatId;
            return this;
        }

        public LocationResponse build() {
            LocationResponse response = new LocationResponse();
            response.id = id;
            response.name = name;
            response.description = description;
            response.ownerUsername = ownerUsername;
            response.telegramChatId = telegramChatId;
            return response;
        }
    }
}
