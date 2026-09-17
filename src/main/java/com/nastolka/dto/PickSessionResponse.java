package com.nastolka.dto;

import com.nastolka.entity.PickSessionStatus;

import java.time.Instant;
import java.util.List;

public class PickSessionResponse {

    private Long id;
    private Long locationId;
    private PickSessionStatus status;
    private boolean excludeAlreadyPlayed;
    private int targetRemainingCount;
    private Integer requiredBanCount;
    private int banCount;
    private String currentTurnUsername;
    private String createdByUsername;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private Long selectedGameId;
    private String selectedGameName;
    private List<PickSessionParticipantResponse> participants;
    private List<PickSessionCandidateResponse> candidates;

    public PickSessionResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public PickSessionStatus getStatus() {
        return status;
    }

    public void setStatus(PickSessionStatus status) {
        this.status = status;
    }

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

    public Integer getRequiredBanCount() {
        return requiredBanCount;
    }

    public void setRequiredBanCount(Integer requiredBanCount) {
        this.requiredBanCount = requiredBanCount;
    }

    public int getBanCount() {
        return banCount;
    }

    public void setBanCount(int banCount) {
        this.banCount = banCount;
    }

    public String getCurrentTurnUsername() {
        return currentTurnUsername;
    }

    public void setCurrentTurnUsername(String currentTurnUsername) {
        this.currentTurnUsername = currentTurnUsername;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Long getSelectedGameId() {
        return selectedGameId;
    }

    public void setSelectedGameId(Long selectedGameId) {
        this.selectedGameId = selectedGameId;
    }

    public String getSelectedGameName() {
        return selectedGameName;
    }

    public void setSelectedGameName(String selectedGameName) {
        this.selectedGameName = selectedGameName;
    }

    public List<PickSessionParticipantResponse> getParticipants() {
        return participants;
    }

    public void setParticipants(List<PickSessionParticipantResponse> participants) {
        this.participants = participants;
    }

    public List<PickSessionCandidateResponse> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<PickSessionCandidateResponse> candidates) {
        this.candidates = candidates;
    }

    public static class Builder {
        private Long id;
        private Long locationId;
        private PickSessionStatus status;
        private boolean excludeAlreadyPlayed;
        private int targetRemainingCount;
        private Integer requiredBanCount;
        private int banCount;
        private String currentTurnUsername;
        private String createdByUsername;
        private Instant createdAt;
        private Instant startedAt;
        private Instant completedAt;
        private Instant cancelledAt;
        private Long selectedGameId;
        private String selectedGameName;
        private List<PickSessionParticipantResponse> participants;
        private List<PickSessionCandidateResponse> candidates;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder locationId(Long locationId) {
            this.locationId = locationId;
            return this;
        }

        public Builder status(PickSessionStatus status) {
            this.status = status;
            return this;
        }

        public Builder excludeAlreadyPlayed(boolean excludeAlreadyPlayed) {
            this.excludeAlreadyPlayed = excludeAlreadyPlayed;
            return this;
        }

        public Builder targetRemainingCount(int targetRemainingCount) {
            this.targetRemainingCount = targetRemainingCount;
            return this;
        }

        public Builder requiredBanCount(Integer requiredBanCount) {
            this.requiredBanCount = requiredBanCount;
            return this;
        }

        public Builder banCount(int banCount) {
            this.banCount = banCount;
            return this;
        }

        public Builder currentTurnUsername(String currentTurnUsername) {
            this.currentTurnUsername = currentTurnUsername;
            return this;
        }

        public Builder createdByUsername(String createdByUsername) {
            this.createdByUsername = createdByUsername;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder startedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder cancelledAt(Instant cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public Builder selectedGameId(Long selectedGameId) {
            this.selectedGameId = selectedGameId;
            return this;
        }

        public Builder selectedGameName(String selectedGameName) {
            this.selectedGameName = selectedGameName;
            return this;
        }

        public Builder participants(List<PickSessionParticipantResponse> participants) {
            this.participants = participants;
            return this;
        }

        public Builder candidates(List<PickSessionCandidateResponse> candidates) {
            this.candidates = candidates;
            return this;
        }

        public PickSessionResponse build() {
            PickSessionResponse response = new PickSessionResponse();
            response.id = id;
            response.locationId = locationId;
            response.status = status;
            response.excludeAlreadyPlayed = excludeAlreadyPlayed;
            response.targetRemainingCount = targetRemainingCount;
            response.requiredBanCount = requiredBanCount;
            response.banCount = banCount;
            response.currentTurnUsername = currentTurnUsername;
            response.createdByUsername = createdByUsername;
            response.createdAt = createdAt;
            response.startedAt = startedAt;
            response.completedAt = completedAt;
            response.cancelledAt = cancelledAt;
            response.selectedGameId = selectedGameId;
            response.selectedGameName = selectedGameName;
            response.participants = participants;
            response.candidates = candidates;
            return response;
        }
    }
}
