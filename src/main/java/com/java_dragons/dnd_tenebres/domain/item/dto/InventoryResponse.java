package com.java_dragons.dnd_tenebres.domain.item.dto;

import java.util.List;

public record InventoryResponse(
        List<InventoryItemResponse> items,
        int occupiedSlots,
        List<EquipmentResponse> equipment,
        List<ItemSetBonusResponse> activeSetBonuses) {
    public record ItemSetBonusResponse(String setCode, int equippedPieces, List<String> activeBonuses) {}
}
