package com.java_dragons.dnd_tenebres.domain.item.dto;

import java.util.List;

public record InventoryResponse(
        List<InventoryItemResponse> items,
        int occupiedSlots) {
}
