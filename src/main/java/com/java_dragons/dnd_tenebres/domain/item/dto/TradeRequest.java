package com.java_dragons.dnd_tenebres.domain.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class TradeRequest {
    private TradeRequest() {
    }

    public record BuyRequest(
            @NotBlank(message = "Название товара обязательно")
            @Size(max = 100, message = "Название товара не должно быть длиннее 100 символов") String templateName,
            @Positive(message = "Количество должно быть больше нуля") int amount
    ) {}

    public record SellRequest(
            @NotNull(message = "ID предмета обязателен") Long playerItemId,
            @Positive(message = "Количество должно быть больше нуля") int amount
    ) {}
}
