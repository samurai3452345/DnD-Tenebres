package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.ItemUpgradeRequest;
import com.java_dragons.dnd_tenebres.domain.item.service.ItemUpgradeService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/forge")
@RequiredArgsConstructor
public class ForgeController {

    private final ItemUpgradeService itemUpgradeService;

    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeItem(
            @CurrentPlayerId Long playerId,
            @Valid @RequestBody ItemUpgradeRequest request) {

        return ResponseEntity.ok(itemUpgradeService.feedItems(
                playerId,
                request.operationId(),
                request.targetItemId(),
                request.foodItemIds()
        ));
    }
}
