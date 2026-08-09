package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.EquipRequest;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<PlayerItem>> getInventory(@CurrentPlayerId Long playerId) {
        List<PlayerItem> inventory = inventoryService.getPlayerInventory(playerId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/equip/{itemId}")
    public ResponseEntity<?> equipItem(
            @CurrentPlayerId Long playerId,
            @PathVariable Long itemId,
            @RequestBody EquipRequest request) {

        inventoryService.equipItem(playerId, itemId, request.slot());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Предмет успешно экипирован"
        ));
    }

    @PostMapping("/unequip")
    public ResponseEntity<?> unequipItem(
            @CurrentPlayerId Long playerId,
            @RequestBody EquipRequest request) {

        inventoryService.unequipItem(playerId, request.slot());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Предмет снят"
        ));
    }
}