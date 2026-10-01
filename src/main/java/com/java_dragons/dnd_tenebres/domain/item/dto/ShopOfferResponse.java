package com.java_dragons.dnd_tenebres.domain.item.dto;

public record ShopOfferResponse(
        Long offerId,
        Long templateId,
        String name,
        String type,
        String rarity,
        int unitPrice,
        int availableQuantity,
        int minLevel,
        boolean available) {
}
