package com.nastolka.service;

import com.nastolka.dto.CreatePickSessionRequest;
import com.nastolka.dto.PickSessionActionRequest;
import com.nastolka.dto.PickSessionResponse;

import java.util.Optional;

public interface PickSessionService {

    PickSessionResponse create(Long locationId, CreatePickSessionRequest request, String username);

    PickSessionResponse join(Long locationId, Long sessionId, String username);

    PickSessionResponse start(Long locationId, Long sessionId, String username);

    PickSessionResponse submitAction(Long locationId, Long sessionId, PickSessionActionRequest request, String username);

    PickSessionResponse cancel(Long locationId, Long sessionId, String username);

    Optional<PickSessionResponse> getActive(Long locationId, String username);

    PickSessionResponse getById(Long locationId, Long sessionId, String username);
}
