package com.nastolka.service.impl;

import com.nastolka.dto.LocationShareResponse;
import com.nastolka.dto.ShareLocationRequest;
import com.nastolka.dto.UpdateSharePermissionsRequest;
import com.nastolka.entity.Location;
import com.nastolka.entity.LocationShare;
import com.nastolka.entity.Role;
import com.nastolka.entity.User;
import com.nastolka.repository.LocationRepository;
import com.nastolka.repository.LocationShareRepository;
import com.nastolka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationShareServiceImplTest {

    private static final Long LOCATION_ID = 1L;

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationShareRepository locationShareRepository;
    @Mock
    private LocationAccessGuard accessGuard;

    private LocationShareServiceImpl service;
    private User owner;
    private User target;
    private Location location;

    @BeforeEach
    void setUp() {
        service = new LocationShareServiceImpl(locationRepository, userRepository, locationShareRepository, accessGuard);

        owner = User.builder().id(1L).username("owner").role(Role.USER).build();
        target = User.builder().id(2L).username("bob").email("bob@example.com").role(Role.USER).build();

        location = new Location();
        location.setId(LOCATION_ID);
        location.setOwner(owner);

        lenient().when(accessGuard.requireUser("owner")).thenReturn(owner);
        lenient().when(locationRepository.findById(LOCATION_ID)).thenReturn(Optional.of(location));
        lenient().when(locationShareRepository.save(any(LocationShare.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shareLocation_persistsRequestedPermissionFlags() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        ShareLocationRequest request = new ShareLocationRequest();
        request.setUsername("bob");
        request.setCanEditInfo(true);
        request.setCanManageHistory(true);

        LocationShareResponse response = service.shareLocation(LOCATION_ID, request, "owner");

        assertThat(response.isCanEditInfo()).isTrue();
        assertThat(response.isCanManageGames()).isFalse();
        assertThat(response.isCanManageHistory()).isTrue();

        ArgumentCaptor<LocationShare> captor = ArgumentCaptor.forClass(LocationShare.class);
        verify(locationShareRepository).save(captor.capture());
        assertThat(captor.getValue().isCanEditInfo()).isTrue();
        assertThat(captor.getValue().isCanManageGames()).isFalse();
        assertThat(captor.getValue().isCanManageHistory()).isTrue();
    }

    @Test
    void shareLocation_defaultsAllPermissionsToFalse_whenOmitted() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        ShareLocationRequest request = new ShareLocationRequest();
        request.setUsername("bob");

        LocationShareResponse response = service.shareLocation(LOCATION_ID, request, "owner");

        assertThat(response.isCanEditInfo()).isFalse();
        assertThat(response.isCanManageGames()).isFalse();
        assertThat(response.isCanManageHistory()).isFalse();
    }

    @Test
    void shareLocation_rejectsSharingWithOwner() {
        when(userRepository.findByUsername("owner")).thenReturn(Optional.of(owner));
        ShareLocationRequest request = new ShareLocationRequest();
        request.setUsername("owner");

        assertThatThrownBy(() -> service.shareLocation(LOCATION_ID, request, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot share a location with its owner");
    }

    @Test
    void shareLocation_rejectsDuplicateShare() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        when(locationShareRepository.existsByLocationIdAndUserId(LOCATION_ID, target.getId())).thenReturn(true);
        ShareLocationRequest request = new ShareLocationRequest();
        request.setUsername("bob");

        assertThatThrownBy(() -> service.shareLocation(LOCATION_ID, request, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already shared");
    }

    @Test
    void shareLocation_requiresManageAccess() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        ShareLocationRequest request = new ShareLocationRequest();
        request.setUsername("bob");

        service.shareLocation(LOCATION_ID, request, "owner");

        verify(accessGuard).requireManageAccess(location, owner);
    }

    @Test
    void getShares_reflectsMixedPermissionFlagsPerShare() {
        LocationShare fullAccess = new LocationShare();
        fullAccess.setLocation(location);
        fullAccess.setUser(target);
        fullAccess.setCanEditInfo(true);
        fullAccess.setCanManageGames(true);
        fullAccess.setCanManageHistory(true);

        User viewer = User.builder().id(3L).username("carol").email("carol@example.com").role(Role.USER).build();
        LocationShare viewOnly = new LocationShare();
        viewOnly.setLocation(location);
        viewOnly.setUser(viewer);

        when(locationShareRepository.findByLocationId(LOCATION_ID)).thenReturn(List.of(fullAccess, viewOnly));

        List<LocationShareResponse> responses = service.getShares(LOCATION_ID, "owner");

        assertThat(responses).hasSize(2);
        LocationShareResponse bobResponse = responses.stream().filter(r -> r.getUsername().equals("bob")).findFirst().orElseThrow();
        assertThat(bobResponse.isCanEditInfo()).isTrue();
        assertThat(bobResponse.isCanManageGames()).isTrue();
        assertThat(bobResponse.isCanManageHistory()).isTrue();

        LocationShareResponse carolResponse = responses.stream().filter(r -> r.getUsername().equals("carol")).findFirst().orElseThrow();
        assertThat(carolResponse.isCanEditInfo()).isFalse();
        assertThat(carolResponse.isCanManageGames()).isFalse();
        assertThat(carolResponse.isCanManageHistory()).isFalse();
    }

    @Test
    void updateSharePermissions_updatesExistingShare() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(target);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        when(locationShareRepository.findByLocationIdAndUserId(LOCATION_ID, target.getId())).thenReturn(Optional.of(share));

        UpdateSharePermissionsRequest request = new UpdateSharePermissionsRequest();
        request.setCanManageGames(true);

        LocationShareResponse response = service.updateSharePermissions(LOCATION_ID, "bob", request, "owner");

        assertThat(response.isCanManageGames()).isTrue();
        assertThat(response.isCanEditInfo()).isFalse();
        assertThat(share.isCanManageGames()).isTrue();
        verify(accessGuard).requireManageAccess(location, owner);
    }

    @Test
    void updateSharePermissions_returns404_whenTargetUserDoesNotExist() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        UpdateSharePermissionsRequest request = new UpdateSharePermissionsRequest();

        assertThatThrownBy(() -> service.updateSharePermissions(LOCATION_ID, "bob", request, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateSharePermissions_returns404_whenNotShared() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        when(locationShareRepository.findByLocationIdAndUserId(LOCATION_ID, target.getId())).thenReturn(Optional.empty());
        UpdateSharePermissionsRequest request = new UpdateSharePermissionsRequest();

        assertThatThrownBy(() -> service.updateSharePermissions(LOCATION_ID, "bob", request, "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not shared");

        verify(locationShareRepository, never()).save(any(LocationShare.class));
    }

    @Test
    void removeShare_deletesExistingShare() {
        LocationShare share = new LocationShare();
        share.setLocation(location);
        share.setUser(target);
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        when(locationShareRepository.findByLocationIdAndUserId(LOCATION_ID, target.getId())).thenReturn(Optional.of(share));

        service.removeShare(LOCATION_ID, "bob", "owner");

        verify(locationShareRepository).delete(share);
    }

    @Test
    void removeShare_returns404_whenNotShared() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(target));
        when(locationShareRepository.findByLocationIdAndUserId(LOCATION_ID, target.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeShare(LOCATION_ID, "bob", "owner"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not shared");
    }
}
