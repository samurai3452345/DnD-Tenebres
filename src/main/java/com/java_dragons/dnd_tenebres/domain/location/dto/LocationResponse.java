package com.java_dragons.dnd_tenebres.domain.location.dto;

import com.java_dragons.dnd_tenebres.domain.location.model.*;
import java.util.List;

public record LocationResponse(String id, String name, String description, LocationType type,
                               BiomeType biome, int recommendedLevel, LocationEffect effect,
                               boolean cleared, List<String> availableActions,
                               List<ConnectionResponse> connections) {
    public record ConnectionResponse(String id, String name, int recommendedLevel, boolean open, String blockedReason) {}
}
