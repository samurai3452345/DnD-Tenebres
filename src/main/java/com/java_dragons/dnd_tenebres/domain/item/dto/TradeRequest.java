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
            @Size(max = 100, message = "Название товара не должно быть длиннее 100 символов")
            String templateName,

            @Positive(message = "Количество должно быть больше нуля")
            int amount
    ) {}

    public record BuyByIdRequest(
            @NotBlank(message = "ID операции обязателен")
            @Size(max = 100, message = "ID операции не должен быть длиннее 100 символов")
            String operationId,

            @NotNull(message = "ID предложения обязателен")
            Long offerId,

            @Positive(message = "Количество должно быть больше нуля")
            int amount
    ) {}

    public record BuyByIdRequest(
            @NotNull(message = "ID шаблона предмета обязателен") Long templateId,
            @Positive(message = "Количество должно быть больше нуля") int amount
    ) {}

    public record SellRequest(
            @NotBlank(message = "ID операции обязателен")
            @Size(max = 100, message = "ID операции не должен быть длиннее 100 символов")
            String operationId,

            @NotNull(message = "ID предмета обязателен")
            Long playerItemId,

            @Positive(message = "Количество должно быть больше нуля")
            int amount
    ) {}
}