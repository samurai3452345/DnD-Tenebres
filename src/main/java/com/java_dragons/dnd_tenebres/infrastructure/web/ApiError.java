package com.java_dragons.dnd_tenebres.infrastructure.web;

import java.time.Instant;
import java.util.Map;
public record ApiError(Instant timestamp, int status, String code, String message,
                       String path, Map<String, String> fieldErrors) {}
