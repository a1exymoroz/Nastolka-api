package com.nastolka.service;

import com.nastolka.dto.BggSearchResult;
import com.nastolka.dto.CreateExpansionRequest;
import com.nastolka.dto.ExpansionResponse;

import java.util.List;

public interface GameExpansionService {

    List<ExpansionResponse> getExpansions(Long gameId);

    List<BggSearchResult> searchExternal(Long gameId);

    ExpansionResponse createExpansion(Long gameId, CreateExpansionRequest request);

    ExpansionResponse importFromBgg(Long gameId, Long bggId);

    void deleteExpansion(Long gameId, Long expansionId);
}
