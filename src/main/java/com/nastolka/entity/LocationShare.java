package com.nastolka.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "location_shares", uniqueConstraints = @UniqueConstraint(columnNames = {"location_id", "user_id"}))
public class LocationShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Location location;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "can_edit_info", nullable = false)
    private boolean canEditInfo;

    @Column(name = "can_manage_games", nullable = false)
    private boolean canManageGames;

    @Column(name = "can_manage_history", nullable = false)
    private boolean canManageHistory;

    public LocationShare() {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isCanEditInfo() {
        return canEditInfo;
    }

    public void setCanEditInfo(boolean canEditInfo) {
        this.canEditInfo = canEditInfo;
    }

    public boolean isCanManageGames() {
        return canManageGames;
    }

    public void setCanManageGames(boolean canManageGames) {
        this.canManageGames = canManageGames;
    }

    public boolean isCanManageHistory() {
        return canManageHistory;
    }

    public void setCanManageHistory(boolean canManageHistory) {
        this.canManageHistory = canManageHistory;
    }
}
