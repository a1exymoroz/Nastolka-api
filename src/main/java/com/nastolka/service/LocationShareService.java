package com.nastolka.service;

import com.nastolka.dto.LocationShareResponse;
import com.nastolka.dto.ShareLocationRequest;
import com.nastolka.dto.UpdateSharePermissionsRequest;

import java.util.List;

public interface LocationShareService {

    List<LocationShareResponse> getShares(Long locationId, String username);

    LocationShareResponse shareLocation(Long locationId, ShareLocationRequest request, String username);

    LocationShareResponse updateSharePermissions(Long locationId, String targetUsername, UpdateSharePermissionsRequest request, String username);

    void removeShare(Long locationId, String targetUsername, String username);
}
