package com.nastolka.dto;

import jakarta.validation.constraints.NotBlank;

public class ShareLocationRequest {

    @NotBlank
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
