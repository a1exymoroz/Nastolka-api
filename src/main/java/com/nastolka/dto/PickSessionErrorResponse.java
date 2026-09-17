package com.nastolka.dto;

public class PickSessionErrorResponse {

    private String message;

    public PickSessionErrorResponse() {
    }

    public PickSessionErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
