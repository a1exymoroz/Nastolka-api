package com.nastolka.service;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.HistoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LocationHistoryService {

    List<HistoryResponse> getHistory(Long locationId, String username);

    HistoryResponse addHistory(Long locationId, CreateHistoryRequest request, String username);

    HistoryResponse updateHistory(Long locationId, Long historyId, CreateHistoryRequest request, String username);

    void deleteHistory(Long locationId, Long historyId, String username);

    HistoryResponse uploadPhoto(Long locationId, Long historyId, MultipartFile file, String username);

    void deletePhoto(Long locationId, Long historyId, String username);

    HistoryPhotoFile getPhoto(Long locationId, Long historyId, String username);
}
