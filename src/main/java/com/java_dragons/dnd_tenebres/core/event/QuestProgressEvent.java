package com.java_dragons.dnd_tenebres.core.event;

import com.java_dragons.dnd_tenebres.domain.quest.model.QuestType;

public record QuestProgressEvent(Long playerId, QuestType type, String targetIdentifier, int amount) {}
