package com.nastolka.service.impl;

import com.nastolka.entity.Location;
import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationShareRepository;
import com.nastolka.repository.UserRepository;
import com.nastolka.security.JwtAuthFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LocationAccessGuard {

    private final UserRepository userRepository;
    private final LocationShareRepository locationShareRepository;

    public LocationAccessGuard(UserRepository userRepository, LocationShareRepository locationShareRepository) {
        this.userRepository = userRepository;
        this.locationShareRepository = locationShareRepository;
    }

    public User requireUser(String username) {
        User requestUser = currentRequestUser();
        if (requestUser != null && requestUser.getUsername().equals(username)) {
            return requestUser;
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    // Avoids a second DB round trip on every REST request; JwtAuthFilter already loaded the User.
    // Not available for WebSocket/STOMP calls (no servlet request in scope there), which fall back to the query above.
    private User currentRequestUser() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return (User) attributes.getAttribute(JwtAuthFilter.AUTHENTICATED_USER_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
    }

    public boolean canManage(Location location, User requester) {
        return requester.getRole() == Role.ADMIN || location.getOwner().getId().equals(requester.getId());
    }

    public void requireManageAccess(Location location, User requester) {
        if (!canManage(location, requester)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this location");
        }
    }

    public void requireViewAccess(Location location, User requester) {
        if (canManage(location, requester)) {
            return;
        }
        boolean shared = locationShareRepository.existsByLocationIdAndUserId(location.getId(), requester.getId());
        if (!shared) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this location");
        }
    }
}
