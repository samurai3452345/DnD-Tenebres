package com.java_dragons.dnd_tenebres.domain.item.dto;

import java.util.Map;

public record ItemComparisonResponse(InventoryItemResponse candidate, InventoryItemResponse equipped,
                                     Map<String, Integer> statDifference) {}
