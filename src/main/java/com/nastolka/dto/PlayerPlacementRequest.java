package com.nastolka.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PlayerPlacementRequest {

    @NotBlank
    private String username;

    @Min(1)
    private Integer placement;

    private Integer points;

    @Size(max = 255)
    private String meeples;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getPlacement() {
        return placement;
    }

    public void setPlacement(Integer placement) {
        this.placement = placement;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getMeeples() {
        return meeples;
    }

    public void setMeeples(String meeples) {
        this.meeples = meeples;
    }
}
