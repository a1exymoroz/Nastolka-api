package com.nastolka.dto;

public class UpdateSharePermissionsRequest {

    private boolean canEditInfo;
    private boolean canManageGames;
    private boolean canManageHistory;

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
}
