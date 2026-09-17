package com.nastolka.dto;

public class LocationShareResponse {

    private String username;
    private String email;
    private boolean canEditInfo;
    private boolean canManageGames;
    private boolean canManageHistory;

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

    public boolean isCanEditInfo() {
        return canEditInfo;
    }

    public void setCanEditInfo(boolean canEditInfo) {
        this.canEditInfo = canEditInfo;
    }

    public boolean isCanManageGames() {
        return canManageGames;
    }

    public void setCanManageGames(boolean canManageGames) {
        this.canManageGames = canManageGames;
    }

    public boolean isCanManageHistory() {
        return canManageHistory;
    }

    public void setCanManageHistory(boolean canManageHistory) {
        this.canManageHistory = canManageHistory;
    }

    public static class Builder {
        private String username;
        private String email;
        private boolean canEditInfo;
        private boolean canManageGames;
        private boolean canManageHistory;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder canEditInfo(boolean canEditInfo) {
            this.canEditInfo = canEditInfo;
            return this;
        }

        public Builder canManageGames(boolean canManageGames) {
            this.canManageGames = canManageGames;
            return this;
        }

        public Builder canManageHistory(boolean canManageHistory) {
            this.canManageHistory = canManageHistory;
            return this;
        }

        public LocationShareResponse build() {
            LocationShareResponse response = new LocationShareResponse();
            response.username = username;
            response.email = email;
            response.canEditInfo = canEditInfo;
            response.canManageGames = canManageGames;
            response.canManageHistory = canManageHistory;
            return response;
        }
    }
}
