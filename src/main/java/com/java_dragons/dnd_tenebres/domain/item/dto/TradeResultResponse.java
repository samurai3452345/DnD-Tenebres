package com.java_dragons.dnd_tenebres.domain.item.dto;

public record TradeResultResponse(
        String operationId,
        String operationType,
        Long resourceId,
        int amount,
        String message,
        boolean alreadyProcessed
) {
}
