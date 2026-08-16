package com.java_dragons.dnd_tenebres.core.event;

public record ItemLootedEvent(Long playerId, String itemTemplateName, int amount) {
}
