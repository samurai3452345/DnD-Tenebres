package com.java_dragons.dnd_tenebres.infrastructure.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class ApiErrorWriter {
    private ApiErrorWriter() {}

    public static void write(HttpServletResponse response, int status, String code,
                             String message, String path) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status
                + ",\"code\":\"" + escape(code) + "\",\"message\":\"" + escape(message)
                + "\",\"path\":\"" + escape(path) + "\",\"fieldErrors\":{}}");
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
