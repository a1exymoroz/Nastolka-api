package com.nastolka.service.impl;

import com.nastolka.entity.Location;
import com.nastolka.entity.LocationShare;
import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationShareRepository;
import com.nastolka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationAccessGuardTest {

    private static final Long LOCATION_ID = 1L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationShareRepository locationShareRepository;

    private LocationAccessGuard guard;
    private User owner;
    private User admin;
    private User sharedUser;
    private User strangerUser;
    private Location location;

    @BeforeEach
    void setUp() {
        guard = new LocationAccessGuard(userRepository, locationShareRepository);

        owner = User.builder().id(1L).username("owner").role(Role.USER).build();
        admin = User.builder().id(2L).username("admin").role(Role.ADMIN).build();
        sharedUser = User.builder().id(3L).username("bob").role(Role.USER).build();
        strangerUser = User.builder().id(4L).username("stranger").role(Role.USER).build();

        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(owner);
    }

    private void stubShare(LocationShare share) {
        when(locationShareRepository.findByLocationIdAndUserId(LOCATION_ID, sharedUser.getId()))
                .thenReturn(Optional.of(share));
    }

    private void stubNoShare() {
        when(locationShareRepository.findByLocationIdAndUserId(LOCATION_ID, strangerUser.getId()))
                .thenReturn(Optional.empty());
    }

    // requireInfoEditAccess

    @Test
    void requireInfoEditAccess_allowsOwnerAndAdmin() {
        assertThatCode(() -> guard.requireInfoEditAccess(location, owner)).doesNotThrowAnyException();
        assertThatCode(() -> guard.requireInfoEditAccess(location, admin)).doesNotThrowAnyException();
    }

    @Test
    void requireInfoEditAccess_allowsSharedUser_whenCanEditInfoGranted() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(sharedUser);
        share.setCanEditInfo(true);
        stubShare(share);

        assertThatCode(() -> guard.requireInfoEditAccess(location, sharedUser)).doesNotThrowAnyException();
    }

    @Test
    void requireInfoEditAccess_deniesSharedUser_whenCanEditInfoNotGranted() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(sharedUser);
        stubShare(share);

        assertThatThrownBy(() -> guard.requireInfoEditAccess(location, sharedUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void requireInfoEditAccess_deniesSharedUser_whenOnlyADifferentPermissionIsGranted() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(sharedUser);
        share.setCanManageGames(true);
        share.setCanManageHistory(true);
        stubShare(share);

        assertThatThrownBy(() -> guard.requireInfoEditAccess(location, sharedUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void requireInfoEditAccess_deniesUserWithNoShareAtAll() {
        stubNoShare();

        assertThatThrownBy(() -> guard.requireInfoEditAccess(location, strangerUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    // requireGamesManageAccess

    @Test
    void requireGamesManageAccess_allowsOwnerAndAdmin() {
        assertThatCode(() -> guard.requireGamesManageAccess(location, owner)).doesNotThrowAnyException();
        assertThatCode(() -> guard.requireGamesManageAccess(location, admin)).doesNotThrowAnyException();
    }

    @Test
    void requireGamesManageAccess_allowsSharedUser_whenCanManageGamesGranted() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(sharedUser);
        share.setCanManageGames(true);
        stubShare(share);

        assertThatCode(() -> guard.requireGamesManageAccess(location, sharedUser)).doesNotThrowAnyException();
    }

    @Test
    void requireGamesManageAccess_deniesSharedUser_whenCanManageGamesNotGranted() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(sharedUser);
        stubShare(share);

        assertThatThrownBy(() -> guard.requireGamesManageAccess(location, sharedUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void requireGamesManageAccess_deniesUserWithNoShareAtAll() {
        stubNoShare();

        assertThatThrownBy(() -> guard.requireGamesManageAccess(location, strangerUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    // requireHistoryManageAccess

    @Test
    void requireHistoryManageAccess_allowsOwnerAndAdmin() {
        assertThatCode(() -> guard.requireHistoryManageAccess(location, owner)).doesNotThrowAnyException();
        assertThatCode(() -> guard.requireHistoryManageAccess(location, admin)).doesNotThrowAnyException();
    }

    @Test
    void requireHistoryManageAccess_allowsSharedUser_whenCanManageHistoryGranted() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(sharedUser);
        share.setCanManageHistory(true);
        stubShare(share);

        assertThatCode(() -> guard.requireHistoryManageAccess(location, sharedUser)).doesNotThrowAnyException();
    }

    @Test
    void requireHistoryManageAccess_deniesSharedUser_whenCanManageHistoryNotGranted() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(sharedUser);
        stubShare(share);

        assertThatThrownBy(() -> guard.requireHistoryManageAccess(location, sharedUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void requireHistoryManageAccess_deniesUserWithNoShareAtAll() {
        stubNoShare();

        assertThatThrownBy(() -> guard.requireHistoryManageAccess(location, strangerUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    // requireViewAccess (regression: unaffected by the new granular flags)

    @Test
    void requireViewAccess_stillAllowsAnySharedUser_regardlessOfGranularFlags() {
        when(locationShareRepository.existsByLocationIdAndUserId(LOCATION_ID, sharedUser.getId()))
                .thenReturn(true);

        assertThatCode(() -> guard.requireViewAccess(location, sharedUser)).doesNotThrowAnyException();
    }
}
