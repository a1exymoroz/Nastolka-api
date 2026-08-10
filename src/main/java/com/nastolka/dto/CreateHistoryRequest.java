package com.nastolka.dto;

import com.nastolka.entity.HistoryState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public class CreateHistoryRequest {

    @NotNull
    private Long gameId;

    private Instant playedAt;

    @NotNull
    private HistoryState state;

    private Instant startedAt;

    private Instant finishedAt;

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

    public Instant getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Instant playedAt) {
        this.playedAt = playedAt;
    }

    public HistoryState getState() {
        return state;
    }

    public void setState(HistoryState state) {
        this.state = state;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
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
