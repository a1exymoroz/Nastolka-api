package com.nastolka.dto;

import com.nastolka.entity.PickSessionCandidateAction;
import jakarta.validation.constraints.NotNull;

public class PickSessionActionRequest {

    @NotNull
    private Long gameId;

    @NotNull
    private PickSessionCandidateAction action;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public PickSessionCandidateAction getAction() {
        return action;
    }

    public void setAction(PickSessionCandidateAction action) {
        this.action = action;
    }
}
