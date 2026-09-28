package com.java_dragons.dnd_tenebres.infrastructure.web;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ApiErrorResponse of(int status, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, message, path, Map.of());
    }
}
