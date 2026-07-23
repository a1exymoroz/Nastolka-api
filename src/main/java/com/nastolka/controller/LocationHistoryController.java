package com.nastolka.controller;

import com.nastolka.dto.CreateHistoryRequest;
import com.nastolka.dto.HistoryResponse;
import com.nastolka.service.HistoryPhotoFile;
import com.nastolka.service.LocationHistoryService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/locations/{locationId}/history")
public class LocationHistoryController {

    private final LocationHistoryService locationHistoryService;

    public LocationHistoryController(LocationHistoryService locationHistoryService) {
        this.locationHistoryService = locationHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<HistoryResponse>> getHistory(
            @PathVariable Long locationId,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationHistoryService.getHistory(locationId, username));
    }

    @PostMapping
    public ResponseEntity<HistoryResponse> addHistory(
            @PathVariable Long locationId,
            @Valid @RequestBody CreateHistoryRequest request,
            @AuthenticationPrincipal String username
    ) {
        HistoryResponse history = locationHistoryService.addHistory(locationId, request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(history);
    }

    @PutMapping("/{historyId}")
    public ResponseEntity<HistoryResponse> updateHistory(
            @PathVariable Long locationId,
            @PathVariable Long historyId,
            @Valid @RequestBody CreateHistoryRequest request,
            @AuthenticationPrincipal String username
    ) {
        HistoryResponse history = locationHistoryService.updateHistory(locationId, historyId, request, username);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<Void> deleteHistory(
            @PathVariable Long locationId,
            @PathVariable Long historyId,
            @AuthenticationPrincipal String username
    ) {
        locationHistoryService.deleteHistory(locationId, historyId, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{historyId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HistoryResponse> uploadPhoto(
            @PathVariable Long locationId,
            @PathVariable Long historyId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String username
    ) {
        return ResponseEntity.ok(locationHistoryService.uploadPhoto(locationId, historyId, file, username));
    }

    @GetMapping("/{historyId}/photo")
    public ResponseEntity<Resource> getPhoto(
            @PathVariable Long locationId,
            @PathVariable Long historyId,
            @AuthenticationPrincipal String username
    ) {
        HistoryPhotoFile photo = locationHistoryService.getPhoto(locationId, historyId, username);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .body(photo.resource());
    }

    @DeleteMapping("/{historyId}/photo")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable Long locationId,
            @PathVariable Long historyId,
            @AuthenticationPrincipal String username
    ) {
        locationHistoryService.deletePhoto(locationId, historyId, username);
        return ResponseEntity.noContent().build();
    }
}
