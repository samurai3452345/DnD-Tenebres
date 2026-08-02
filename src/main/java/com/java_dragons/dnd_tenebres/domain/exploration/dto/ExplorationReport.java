package com.java_dragons.dnd_tenebres.domain.exploration.dto;

import com.java_dragons.dnd_tenebres.domain.exploration.model.ExplorationEventType;
import java.util.List;

public record ExplorationReport(
        ExplorationEventType eventType,
        String message,
        Long encounterMonsterId,
        List<String> foundItems
) {
    public static ExplorationReport moved(String locationName) {
        return new ExplorationReport(ExplorationEventType.MOVED, "Вы перешли в: " + locationName, null, null);
    }

    public static ExplorationReport combat(String message, Long monsterId) {
        return new ExplorationReport(ExplorationEventType.COMBAT_STARTED, message, monsterId, null);
    }

    public static ExplorationReport loot(String message, List<String> items) {
        return new ExplorationReport(ExplorationEventType.FOUND_LOOT, message, null, items);
    }

    public static ExplorationReport nothing(String message) {
        return new ExplorationReport(ExplorationEventType.NOTHING_FOUND, message, null, null);
    }
}