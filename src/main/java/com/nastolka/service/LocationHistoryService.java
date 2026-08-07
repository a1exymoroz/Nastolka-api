package com.nastolka.service;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.HistoryResponse;

import java.util.List;

public interface LocationHistoryService {

    List<HistoryResponse> getHistory(Long locationId, String username);

    List<HistoryResponse> getRecentHistoryByChatId(String telegramChatId, int limit);

    HistoryResponse addHistory(Long locationId, CreateHistoryRequest request, String username);

    HistoryResponse updateHistory(Long locationId, Long historyId, CreateHistoryRequest request, String username);

    void deleteHistory(Long locationId, Long historyId, String username);
}
