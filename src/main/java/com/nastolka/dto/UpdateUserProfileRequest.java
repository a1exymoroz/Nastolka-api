package com.nastolka.dto;

import jakarta.validation.constraints.Size;

public class UpdateUserProfileRequest {

    @Size(min = 1, max = 50)
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
