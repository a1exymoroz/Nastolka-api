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
@Table(name = "location_game_expansions", uniqueConstraints = @UniqueConstraint(columnNames = {"location_game_id", "expansion_id"}))
public class LocationGameExpansion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_game_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LocationGame locationGame;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expansion_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GameExpansion expansion;

    public LocationGameExpansion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocationGame getLocationGame() {
        return locationGame;
    }

    public void setLocationGame(LocationGame locationGame) {
        this.locationGame = locationGame;
    }

    public GameExpansion getExpansion() {
        return expansion;
    }

    public void setExpansion(GameExpansion expansion) {
        this.expansion = expansion;
    }
}
