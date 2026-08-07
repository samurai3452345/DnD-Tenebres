package com.java_dragons.dnd_tenebres.domain.item.dto;

public class TradeRequest {
    public record BuyRequest(String templateName, int amount) {}
    public record SellRequest(Long playerItemId, int amount) {}
}