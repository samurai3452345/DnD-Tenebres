package com.java_dragons.dnd_tenebres.domain.combat.controller;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatActionRequest;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatReport;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatStateResponse;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatTurnRequest;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatEncounterService;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/combat")
@RequiredArgsConstructor
public class CombatController {

    private final CombatService combatService;
    private final CombatEncounterService encounterService;

    @GetMapping("/current")
    public ResponseEntity<CombatStateResponse> current(
            @CurrentPlayerId Long playerId
    ) {
        return ResponseEntity.ok(encounterService.current(playerId));
    }

    @PostMapping("/actions")
    public ResponseEntity<CombatStateResponse> action(
            @CurrentPlayerId Long playerId,
            @Valid @RequestBody CombatActionRequest request
    ) {
        return ResponseEntity.ok(
                encounterService.executeAction(playerId, request)
        );
    }

    @PostMapping("/flee")
    public ResponseEntity<CombatStateResponse> flee(
            @CurrentPlayerId Long playerId
    ) {
        return ResponseEntity.ok(encounterService.flee(playerId));
    }

    @Deprecated
    @PostMapping("/turn")
    public ResponseEntity<CombatReport> executeTurn(
            @CurrentPlayerId Long playerId,
            @Valid @RequestBody CombatTurnRequest request
    ) {
        return ResponseEntity.ok(
                combatService.executeTurnByIds(playerId, request)
        );
    }

    @GetMapping("/journal")
    public ResponseEntity<Page<CombatEvent>> journal(
            @CurrentPlayerId Long playerId,
            @RequestParam(required = false) Long encounterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(
                encounterService.journal(playerId, encounterId, page, size)
        );
    }
}