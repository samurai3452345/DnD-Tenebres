package com.java_dragons.dnd_tenebres.domain.location.dto;

import com.java_dragons.dnd_tenebres.domain.location.model.*;
import java.util.List;

public record LocationResponse(String id, String name, String zoneName, String description, LocationType type,
                               BiomeType biome, int recommendedLevel, LocationEffect effect,
                               boolean cleared, boolean bossRoom, List<String> availableActions,
                               List<ConnectionResponse> connections,
                               List<ResourceResponse> resources) {
    public record ConnectionResponse(String id, String name, int recommendedLevel, boolean open,
                                     List<String> blockedReasons) {}
    public record ResourceResponse(Long templateId, String name, int minAmount, int maxAmount, int findChance) {}
}
