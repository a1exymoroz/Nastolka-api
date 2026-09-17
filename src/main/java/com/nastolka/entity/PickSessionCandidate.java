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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "pick_session_candidates", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "game_id"}))
public class PickSessionCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PickSession session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Game game;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'UNDECIDED'")
    @Column(nullable = false)
    private PickSessionCandidateAction action = PickSessionCandidateAction.UNDECIDED;

    @ManyToOne
    @JoinColumn(name = "acted_by_id")
    private User actedBy;

    @Column(name = "acted_at")
    private Instant actedAt;

    @Column(name = "action_sequence")
    private Integer actionSequence;

    public PickSessionCandidate() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PickSession getSession() {
        return session;
    }

    public void setSession(PickSession session) {
        this.session = session;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public PickSessionCandidateAction getAction() {
        return action;
    }

    public void setAction(PickSessionCandidateAction action) {
        this.action = action;
    }

    public User getActedBy() {
        return actedBy;
    }

    public void setActedBy(User actedBy) {
        this.actedBy = actedBy;
    }

    public Instant getActedAt() {
        return actedAt;
    }

    public void setActedAt(Instant actedAt) {
        this.actedAt = actedAt;
    }

    public Integer getActionSequence() {
        return actionSequence;
    }

    public void setActionSequence(Integer actionSequence) {
        this.actionSequence = actionSequence;
    }
}
