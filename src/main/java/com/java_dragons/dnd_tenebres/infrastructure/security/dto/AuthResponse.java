package com.java_dragons.dnd_tenebres.infrastructure.security.dto;

public record AuthResponse(
        String token,
        boolean hasCharacter,
        long characterCount,
        Long selectedPlayerId
) {
}
