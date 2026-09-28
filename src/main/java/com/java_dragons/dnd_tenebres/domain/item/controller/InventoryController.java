package com.java_dragons.dnd_tenebres.domain.item.controller;

import com.java_dragons.dnd_tenebres.domain.item.dto.EquipRequest;
import com.java_dragons.dnd_tenebres.domain.item.dto.InventoryItemResponse;
import com.java_dragons.dnd_tenebres.domain.item.dto.InventoryResponse;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<InventoryResponse> getInventory(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(inventoryService.getInventoryResponse(playerId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponse> getItem(@CurrentPlayerId Long playerId, @PathVariable Long itemId) {
        return ResponseEntity.ok(inventoryService.getItemResponse(playerId, itemId));
    }

    @PostMapping("/{itemId}/equip")
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

    @PostMapping("/slots/{slot}/unequip")
    public ResponseEntity<?> unequipItem(
            @CurrentPlayerId Long playerId,
            @PathVariable com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot slot) {

        inventoryService.unequipItem(playerId, slot);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Предмет снят"
        ));
    }


    @PostMapping("/{itemId}/lock")
    public ResponseEntity<?> lock(@CurrentPlayerId Long playerId, @PathVariable Long itemId) {
        inventoryService.setLocked(playerId, itemId, true);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Предмет заблокирован"));
    }

    @PostMapping("/{itemId}/unlock")
    public ResponseEntity<?> unlock(@CurrentPlayerId Long playerId, @PathVariable Long itemId) {
        inventoryService.setLocked(playerId, itemId, false);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "message", "Блокировка снята"));
    }
}
