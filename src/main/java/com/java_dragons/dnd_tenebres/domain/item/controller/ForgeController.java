package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.ItemUpgradeRequest;
import com.java_dragons.dnd_tenebres.domain.item.service.ItemUpgradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/forge")
@RequiredArgsConstructor
public class ForgeController {

    private final ItemUpgradeService itemUpgradeService;

    @PostMapping("/{playerId}/upgrade")
    public ResponseEntity<?> upgradeItem(
            @PathVariable Long playerId,
            @RequestBody ItemUpgradeRequest request) {

        itemUpgradeService.feedItems(playerId, request.targetItemId(), request.foodItemIds());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Предмет успешно поглотил энергию скормленных вещей!"
        ));
    }
}