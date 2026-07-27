package com.nastolka.service;

import com.nastolka.dto.ExpansionResponse;

import java.util.List;

public interface LocationGameExpansionService {

    List<ExpansionResponse> getExpansions(Long locationId, Long gameId, String username);

    ExpansionResponse addExpansion(Long locationId, Long gameId, Long expansionId, String username);

    ExpansionResponse importExpansion(Long locationId, Long gameId, Long bggId, String username);

    void removeExpansion(Long locationId, Long gameId, Long expansionId, String username);
}
