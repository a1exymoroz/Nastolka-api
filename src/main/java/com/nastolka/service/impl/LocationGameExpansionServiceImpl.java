package com.nastolka.service.impl;

import com.nastolka.dto.ExpansionResponse;
import com.nastolka.entity.GameExpansion;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationGame;
import com.nastolka.entity.LocationGameExpansion;
import com.nastolka.entity.User;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.LocationGameExpansionRepository;
import com.nastolka.repository.LocationGameRepository;
import com.nastolka.repository.LocationRepository;
import com.nastolka.service.LocationGameExpansionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LocationGameExpansionServiceImpl implements LocationGameExpansionService {

    private static final String BGG_GAME_URL_TEMPLATE = "https://boardgamegeek.com/boardgame/%d";

    private final LocationRepository locationRepository;
    private final LocationGameRepository locationGameRepository;
    private final GameExpansionRepository expansionRepository;
    private final LocationGameExpansionRepository locationGameExpansionRepository;
    private final LocationAccessGuard accessGuard;

    public LocationGameExpansionServiceImpl(
            LocationRepository locationRepository,
            LocationGameRepository locationGameRepository,
            GameExpansionRepository expansionRepository,
            LocationGameExpansionRepository locationGameExpansionRepository,
            LocationAccessGuard accessGuard
    ) {
        this.locationRepository = locationRepository;
        this.locationGameRepository = locationGameRepository;
        this.expansionRepository = expansionRepository;
        this.locationGameExpansionRepository = locationGameExpansionRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    public List<ExpansionResponse> getExpansions(Long locationId, Long gameId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireViewAccess(location, requester);

        LocationGame locationGame = requireLocationGame(locationId, gameId);
        return locationGameExpansionRepository.findByLocationGameId(locationGame.getId()).stream()
                .map(LocationGameExpansion::getExpansion)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ExpansionResponse addExpansion(Long locationId, Long gameId, Long expansionId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        LocationGame locationGame = requireLocationGame(locationId, gameId);
        GameExpansion expansion = expansionRepository.findById(expansionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expansion not found"));

        if (!expansion.getGame().getId().equals(gameId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expansion does not belong to this game");
        }

        if (locationGameExpansionRepository.existsByLocationGameIdAndExpansionId(locationGame.getId(), expansionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Expansion already added to this location");
        }

        LocationGameExpansion locationGameExpansion = new LocationGameExpansion();
        locationGameExpansion.setLocationGame(locationGame);
        locationGameExpansion.setExpansion(expansion);
        locationGameExpansionRepository.save(locationGameExpansion);

        return toResponse(expansion);
    }

    @Override
    public void removeExpansion(Long locationId, Long gameId, Long expansionId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        LocationGame locationGame = requireLocationGame(locationId, gameId);
        LocationGameExpansion locationGameExpansion = locationGameExpansionRepository
                .findByLocationGameIdAndExpansionId(locationGame.getId(), expansionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expansion not found in this location"));
        locationGameExpansionRepository.delete(locationGameExpansion);
    }

    private Location requireLocation(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private LocationGame requireLocationGame(Long locationId, Long gameId) {
        return locationGameRepository.findByLocationIdAndGameId(locationId, gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found in this location"));
    }

    private ExpansionResponse toResponse(GameExpansion expansion) {
        return ExpansionResponse.builder()
                .id(expansion.getId())
                .gameId(expansion.getGame().getId())
                .bggId(expansion.getBggId())
                .name(expansion.getName())
                .description(expansion.getDescription())
                .photo(expansion.getPhoto())
                .bggUrl(expansion.getBggId() != null ? BGG_GAME_URL_TEMPLATE.formatted(expansion.getBggId()) : null)
                .build();
    }
}
