package com.java_dragons.dnd_tenebres.domain.item.dto;

public record ShopOfferResponse(
        Long templateId,
        String name,
        String type,
        String rarity,
        int unitPrice) {
}
