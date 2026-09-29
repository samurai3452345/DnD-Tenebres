package com.java_dragons.dnd_tenebres.domain.item.dto;

import java.util.List;

public record ItemUpgradePreviewResponse(Long targetItemId, long currentXp, int gainedXp,
                                         long resultingXp, int currentTier, int resultingTier,
                                         int requiredHeroLevel, List<Long> consumedItemIds,
                                         List<String> warnings, boolean alreadyProcessed) {}
