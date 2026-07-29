package com.nastolka.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long id;
    private Long locationId;
    private String senderUsername;
    private boolean senderAdmin;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {
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

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public boolean isSenderAdmin() {
        return senderAdmin;
    }

    public void setSenderAdmin(boolean senderAdmin) {
        this.senderAdmin = senderAdmin;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class Builder {
        private Long id;
        private Long locationId;
        private String senderUsername;
        private boolean senderAdmin;
        private String content;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public Builder senderUsername(String senderUsername) {
            this.senderUsername = senderUsername;
            return this;
        }

        public Builder senderAdmin(boolean senderAdmin) {
            this.senderAdmin = senderAdmin;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ChatMessageResponse build() {
            ChatMessageResponse response = new ChatMessageResponse();
            response.id = id;
            response.locationId = locationId;
            response.senderUsername = senderUsername;
            response.senderAdmin = senderAdmin;
            response.content = content;
            response.createdAt = createdAt;
            return response;
        }
    }
}
