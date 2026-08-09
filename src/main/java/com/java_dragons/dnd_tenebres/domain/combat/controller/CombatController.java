package com.java_dragons.dnd_tenebres.domain.combat.controller;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatReport;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatTurnRequest;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/combat")
@RequiredArgsConstructor
public class CombatController {

    private final CombatService combatService;

    @PostMapping("/turn")
    public ResponseEntity<CombatReport> executeTurn(
            @CurrentPlayerId Long playerId,
            @RequestBody CombatTurnRequest request) {

        CombatReport report = combatService.executeTurnByIds(playerId, request);

        return ResponseEntity.ok(report);
    }
}