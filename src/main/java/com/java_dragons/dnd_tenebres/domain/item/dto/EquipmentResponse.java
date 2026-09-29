package com.java_dragons.dnd_tenebres.domain.item.dto;

import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;

public record EquipmentResponse(EquipmentSlot slot, InventoryItemResponse item) {}
