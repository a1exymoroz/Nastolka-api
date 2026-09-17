package com.nastolka.dto;

import jakarta.validation.constraints.Min;

public class CreatePickSessionRequest {

    private boolean excludeAlreadyPlayed;

    @Min(1)
    private int targetRemainingCount;

    public boolean isExcludeAlreadyPlayed() {
        return excludeAlreadyPlayed;
    }

    public void setExcludeAlreadyPlayed(boolean excludeAlreadyPlayed) {
        this.excludeAlreadyPlayed = excludeAlreadyPlayed;
    }

    public int getTargetRemainingCount() {
        return targetRemainingCount;
    }

    public void setTargetRemainingCount(int targetRemainingCount) {
        this.targetRemainingCount = targetRemainingCount;
    }
}
