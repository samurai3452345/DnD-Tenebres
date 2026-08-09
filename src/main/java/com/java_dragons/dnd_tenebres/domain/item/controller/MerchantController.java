package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.TradeRequest;
import com.java_dragons.dnd_tenebres.domain.item.service.ShopService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final ShopService shopService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyItem(
            @CurrentPlayerId Long playerId,
            @RequestBody TradeRequest.BuyRequest request) {

        String result = shopService.buyItem(playerId, request.templateName(), request.amount());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", result
        ));
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellItem(
            @CurrentPlayerId Long playerId,
            @RequestBody TradeRequest.SellRequest request) {

        String result = shopService.sellItem(playerId, request.playerItemId(), request.amount());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", result
        ));
    }
}