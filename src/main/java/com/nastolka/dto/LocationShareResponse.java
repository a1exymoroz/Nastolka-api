package com.nastolka.dto;

public class LocationShareResponse {

    private String username;
    private String email;

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

    public static class Builder {
        private String username;
        private String email;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public LocationShareResponse build() {
            LocationShareResponse response = new LocationShareResponse();
            response.username = username;
            response.email = email;
            return response;
        }
    }
}
