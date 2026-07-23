package com.nastolka.entity;

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
@Table(name = "location_history_expansions", uniqueConstraints = @UniqueConstraint(columnNames = {"history_id", "expansion_id"}))
public class HistoryExpansion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "history_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LocationHistory history;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expansion_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameExpansion expansion;

    public HistoryExpansion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocationHistory getHistory() {
        return history;
    }

    public void setHistory(LocationHistory history) {
        this.history = history;
    }

    public GameExpansion getExpansion() {
        return expansion;
    }

    public void setExpansion(GameExpansion expansion) {
        this.expansion = expansion;
    }
}
