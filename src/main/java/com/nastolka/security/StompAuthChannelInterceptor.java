package com.nastolka.security;

import com.nastolka.entity.Location;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationRepository;
import com.nastolka.service.UserService;
import com.nastolka.service.impl.LocationAccessGuard;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Pattern LOCATION_DESTINATION_PATTERN =
            Pattern.compile("/(?:app|topic)/locations/(\\d+)/chat(?:\\.send)?");

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final LocationRepository locationRepository;
    private final LocationAccessGuard accessGuard;

    public StompAuthChannelInterceptor(
            JwtUtil jwtUtil,
            UserService userService,
            LocationRepository locationRepository,
            LocationAccessGuard accessGuard
    ) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.locationRepository = locationRepository;
        this.accessGuard = accessGuard;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            authenticate(accessor);
        } else if (command == StompCommand.SUBSCRIBE || command == StompCommand.SEND) {
            authorize(accessor);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new MessagingException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        Optional<String> username = jwtUtil.validateAndExtractUsername(token);
        if (username.isEmpty()) {
            throw new MessagingException("Invalid or expired token");
        }

        User user = userService.findByUsername(username.get())
                .orElseThrow(() -> new MessagingException("User not found"));

        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Principal principal = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        accessor.setUser(principal);
    }

    private void authorize(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new MessagingException("Not authenticated");
        }

        String destination = accessor.getDestination();
        Long locationId = extractLocationId(destination);
        if (locationId == null) {
            return;
        }

        User requester = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new MessagingException("User not found"));
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new MessagingException("Location not found"));

        try {
            accessGuard.requireViewAccess(location, requester);
        } catch (ResponseStatusException e) {
            throw new MessagingException("You do not have access to this location's chat", e);
        }
    }

    private Long extractLocationId(String destination) {
        if (destination == null) {
            return null;
        }
        Matcher matcher = LOCATION_DESTINATION_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return null;
        }
        return Long.valueOf(matcher.group(1));
    }
}
