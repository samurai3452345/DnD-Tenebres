package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.MerchantResponse;
import com.java_dragons.dnd_tenebres.domain.item.dto.ShopOfferResponse;
import com.java_dragons.dnd_tenebres.domain.item.dto.TradeRequest;
import com.java_dragons.dnd_tenebres.domain.item.dto.TradeResultResponse;
import com.java_dragons.dnd_tenebres.domain.item.service.ShopService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants/current")
@RequiredArgsConstructor
public class MerchantController {

    private final ShopService shopService;

    @GetMapping
    public ResponseEntity<MerchantResponse> current(
            @CurrentPlayerId Long playerId
    ) {
        return ResponseEntity.ok(
                shopService.currentMerchant(playerId)
        );
    }

    @GetMapping("/offers")
    public ResponseEntity<List<ShopOfferResponse>> offers(
            @CurrentPlayerId Long playerId
    ) {
        return ResponseEntity.ok(
                shopService.getAssortment(playerId)
        );
    }

    @PostMapping("/buy")
    public ResponseEntity<TradeResultResponse> buy(
            @CurrentPlayerId Long playerId,
            @Valid @RequestBody TradeRequest.BuyByIdRequest request
    ) {
        TradeResultResponse result = shopService.buyItem(
                playerId,
                request.operationId(),
                request.offerId(),
                request.amount()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResultResponse> sell(
            @CurrentPlayerId Long playerId,
            @Valid @RequestBody TradeRequest.SellRequest request
    ) {
        TradeResultResponse result = shopService.sellItem(
                playerId,
                request.operationId(),
                request.playerItemId(),
                request.amount()
        );

        return ResponseEntity.ok(result);
    }
}
