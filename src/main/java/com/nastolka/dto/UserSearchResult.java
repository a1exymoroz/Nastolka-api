package com.nastolka.dto;

public class UserSearchResult {

    private String username;

    public UserSearchResult() {
    }

    public UserSearchResult(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
