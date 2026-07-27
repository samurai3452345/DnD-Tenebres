package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.EquipRequest;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
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

    @GetMapping("/{playerId}")
    public ResponseEntity<List<PlayerItem>> getInventory(@PathVariable Long playerId) {
        List<PlayerItem> inventory = inventoryService.getPlayerInventory(playerId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{playerId}/equip/{itemId}")
    public ResponseEntity<?> equipItem(
            @PathVariable Long playerId,
            @PathVariable Long itemId,
            @RequestBody EquipRequest request) {

        inventoryService.equipItem(playerId, itemId, request.slot());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Предмет успешно экипирован"
        ));
    }

    @PostMapping("/{playerId}/unequip")
    public ResponseEntity<?> unequipItem(
            @PathVariable Long playerId,
            @RequestBody EquipRequest request) {

        inventoryService.unequipItem(playerId, request.slot());

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Предмет снят"
        ));
    }
}