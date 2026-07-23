package com.nastolka.service;

import org.springframework.core.io.Resource;

public record HistoryPhotoFile(Resource resource, String contentType) {
}
