package com.nastolka.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "pick_sessions")
public class PickSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Location location;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'WAITING_FOR_PLAYERS'")
    @Column(nullable = false)
    private PickSessionStatus status = PickSessionStatus.WAITING_FOR_PLAYERS;

    @Column(name = "exclude_already_played", nullable = false)
    private boolean excludeAlreadyPlayed;

    @Column(name = "target_remaining_count", nullable = false)
    private int targetRemainingCount;

    @Column(name = "required_ban_count")
    private Integer requiredBanCount;

    @Column(name = "ban_count", nullable = false)
    private int banCount;

    @Column(name = "current_turn_index")
    private Integer currentTurnIndex;

    @ManyToOne
    @JoinColumn(name = "selected_game_id")
    private Game selectedGame;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Version
    private Long version;

    public PickSession() {
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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

    public Integer getCurrentTurnIndex() {
        return currentTurnIndex;
    }

    public void setCurrentTurnIndex(Integer currentTurnIndex) {
        this.currentTurnIndex = currentTurnIndex;
    }

    public Game getSelectedGame() {
        return selectedGame;
    }

    public void setSelectedGame(Game selectedGame) {
        this.selectedGame = selectedGame;
    }

    public Instant getCreatedAt() {
        return createdAt;
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
}
