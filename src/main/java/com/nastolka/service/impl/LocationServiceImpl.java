package com.nastolka.service.impl;

import com.nastolka.dto.CreateLocationRequest;
import com.nastolka.dto.LocationResponse;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationShare;
import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationRepository;
import com.nastolka.repository.LocationShareRepository;
import com.nastolka.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@Service
public class LocationServiceImpl implements LocationService {

    private static final int MAX_LOCATIONS_PER_USER = 10;

    private final LocationRepository locationRepository;
    private final LocationShareRepository locationShareRepository;
    private final LocationAccessGuard accessGuard;

    public LocationServiceImpl(
            LocationRepository locationRepository,
            LocationShareRepository locationShareRepository,
            LocationAccessGuard accessGuard
    ) {
        this.locationRepository = locationRepository;
        this.locationShareRepository = locationShareRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    public List<LocationResponse> getAllLocations(String username) {
        User requester = accessGuard.requireUser(username);

        if (requester.getRole() == Role.ADMIN) {
            return locationRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        }

        List<Location> owned = locationRepository.findByOwnerId(requester.getId());
        List<Location> shared = locationShareRepository.findByUserId(requester.getId()).stream()
                .map(LocationShare::getLocation)
                .toList();

        return Stream.concat(owned.stream(), shared.stream())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LocationResponse getLocationById(Long id, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(id);
        accessGuard.requireViewAccess(location, requester);
        return toResponse(location);
    }

    @Override
    public LocationResponse createLocation(CreateLocationRequest request, String username) {
        User owner = accessGuard.requireUser(username);

        if (locationRepository.countByOwnerId(owner.getId()) >= MAX_LOCATIONS_PER_USER) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have the maximum of " + MAX_LOCATIONS_PER_USER + " locations"
            );
        }

        requireTelegramChatIdAvailable(request.getTelegramChatId());

        Location location = new Location();
        location.setOwner(owner);
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        location.setTelegramChatId(request.getTelegramChatId());

        return toResponse(locationRepository.save(location));
    }

    @Override
    public LocationResponse updateLocation(Long id, CreateLocationRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(id);
        accessGuard.requireManageAccess(location, requester);

        requireTelegramChatIdAvailable(id, request.getTelegramChatId());

        location.setName(request.getName());
        location.setDescription(request.getDescription());
        location.setTelegramChatId(request.getTelegramChatId());

        return toResponse(locationRepository.save(location));
    }

    @Override
    public void deleteLocation(Long id, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(id);
        accessGuard.requireManageAccess(location, requester);
        locationRepository.delete(location);
    }

    private void requireTelegramChatIdAvailable(String telegramChatId) {
        if (telegramChatId != null && !telegramChatId.isBlank()
                && locationRepository.existsByTelegramChatId(telegramChatId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This Telegram chat is already linked to another location");
        }
    }

    private void requireTelegramChatIdAvailable(Long locationId, String telegramChatId) {
        if (telegramChatId != null && !telegramChatId.isBlank()
                && locationRepository.existsByTelegramChatIdAndIdNot(telegramChatId, locationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This Telegram chat is already linked to another location");
        }
    }

    private Location requireLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private LocationResponse toResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .description(location.getDescription())
                .ownerUsername(location.getOwner().getUsername())
                .telegramChatId(location.getTelegramChatId())
                .build();
    }
}
