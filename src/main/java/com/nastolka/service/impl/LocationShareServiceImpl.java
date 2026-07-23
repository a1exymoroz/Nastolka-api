package com.nastolka.service.impl;

import com.nastolka.dto.LocationShareResponse;
import com.nastolka.dto.ShareLocationRequest;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationShare;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationRepository;
import com.nastolka.repository.LocationShareRepository;
import com.nastolka.repository.UserRepository;
import com.nastolka.service.LocationShareService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LocationShareServiceImpl implements LocationShareService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final LocationShareRepository locationShareRepository;
    private final LocationAccessGuard accessGuard;

    public LocationShareServiceImpl(
            LocationRepository locationRepository,
            UserRepository userRepository,
            LocationShareRepository locationShareRepository,
            LocationAccessGuard accessGuard
    ) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.locationShareRepository = locationShareRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    public List<LocationShareResponse> getShares(Long locationId, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        return locationShareRepository.findByLocationId(locationId).stream()
                .map(LocationShare::getUser)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LocationShareResponse shareLocation(Long locationId, ShareLocationRequest request, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        User target = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (target.getId().equals(location.getOwner().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot share a location with its owner");
        }

        if (locationShareRepository.existsByLocationIdAndUserId(locationId, target.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Location already shared with this user");
        }

        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(target);
        locationShareRepository.save(share);

        return toResponse(target);
    }

    @Override
    public void removeShare(Long locationId, String targetUsername, String username) {
        User requester = accessGuard.requireUser(username);
        Location location = requireLocation(locationId);
        accessGuard.requireManageAccess(location, requester);

        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        LocationShare share = locationShareRepository.findByLocationIdAndUserId(locationId, target.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location is not shared with this user"));
        locationShareRepository.delete(share);
    }

    private Location requireLocation(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));
    }

    private LocationShareResponse toResponse(User user) {
        return LocationShareResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
