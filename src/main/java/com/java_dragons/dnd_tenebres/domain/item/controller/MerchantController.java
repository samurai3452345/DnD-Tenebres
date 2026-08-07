package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.TradeRequest;
import com.java_dragons.dnd_tenebres.domain.item.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final ShopService shopService;

    @PostMapping("/{playerId}/buy")
    public ResponseEntity<?> buyItem(@PathVariable Long playerId, @RequestBody TradeRequest.BuyRequest request) {
        String result = shopService.buyItem(playerId, request.templateName(), request.amount());
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", result
        ));
    }

    @PostMapping("/{playerId}/sell")
    public ResponseEntity<?> sellItem(@PathVariable Long playerId, @RequestBody TradeRequest.SellRequest request) {
        String result = shopService.sellItem(playerId, request.playerItemId(), request.amount());
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", result
        ));
    }
}