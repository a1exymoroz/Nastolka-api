package com.nastolka.service;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.HistoryResponse;
import com.nastolka.dto.VoteRequest;

import java.util.List;

public interface LocationHistoryService {

    List<HistoryResponse> getHistory(Long locationId, String username);

    HistoryResponse getHistoryById(Long locationId, Long historyId, String username);

    List<HistoryResponse> getRecentHistoryByChatId(String telegramChatId, int limit);

    HistoryResponse addHistory(Long locationId, CreateHistoryRequest request, String username);

    HistoryResponse updateHistory(Long locationId, Long historyId, CreateHistoryRequest request, String username);

    void deleteHistory(Long locationId, Long historyId, String username);

    HistoryResponse voteOnHistory(Long locationId, Long historyId, VoteRequest request, String username);
}
