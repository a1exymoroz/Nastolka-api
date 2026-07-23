package com.nastolka.dto;

import com.nastolka.entity.HistoryState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class CreateHistoryRequest {

    @NotNull
    private Long gameId;

    private LocalDateTime playedAt;

    @NotNull
    private HistoryState state;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @Min(1)
    @Max(10)
    private Integer rating;

    @NotEmpty
    private List<@Valid PlayerPlacementRequest> players;

    private List<Long> expansionIds;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(LocalDateTime playedAt) {
        this.playedAt = playedAt;
    }

    public HistoryState getState() {
        return state;
    }

    public void setState(HistoryState state) {
        this.state = state;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public List<PlayerPlacementRequest> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerPlacementRequest> players) {
        this.players = players;
    }

    public List<Long> getExpansionIds() {
        return expansionIds;
    }

    public void setExpansionIds(List<Long> expansionIds) {
        this.expansionIds = expansionIds;
    }
}
