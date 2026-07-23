package com.nastolka.service;

import com.nastolka.dto.CreateLocationRequest;
import com.nastolka.dto.LocationResponse;

import java.util.List;

public interface LocationService {

    List<LocationResponse> getAllLocations(String username);

    LocationResponse getLocationById(Long id, String username);

    LocationResponse createLocation(CreateLocationRequest request, String username);

    LocationResponse updateLocation(Long id, CreateLocationRequest request, String username);

    void deleteLocation(Long id, String username);
}
