package com.nastolka.dto;

public class LocationShareResponse {

    private String username;
    private String email;
    private String displayName;

    public LocationShareResponse() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public static class Builder {
        private String username;
        private String email;
        private String displayName;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public LocationShareResponse build() {
            LocationShareResponse response = new LocationShareResponse();
            response.username = username;
            response.email = email;
            response.displayName = displayName;
            return response;
        }
    }
}
