package com.java_dragons.dnd_tenebres.domain.location.controller;

import com.java_dragons.dnd_tenebres.domain.combat.service.CombatEncounterService;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterReason;
import com.java_dragons.dnd_tenebres.domain.location.service.RestingService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.dto.RestReport;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rest")
@RequiredArgsConstructor
public class RestingController {

    private final RestingService restingService;
    private final MonsterSpawnerService monsterSpawnerService;
    private final CombatEncounterService encounterService;

    @PostMapping("/short")
    public ResponseEntity<?> takeShortRest(@CurrentPlayerId Long playerId) {

        RestReport report = restingService.takeShortRest(playerId);

        if (report.isAmbushed()) {

            Monster ambushingMonster = monsterSpawnerService.spawnRandomMonster(report.locationId());
            encounterService.startEncounter(playerId, List.of(ambushingMonster), EncounterReason.REST_AMBUSH);
            var combatState = encounterService.applyAmbushOpening(playerId, ambushingMonster);

            return ResponseEntity.ok(Map.of(
                    "status", "AMBUSH",
                    "message", report.message(),
                    "monster", ambushingMonster.getName(),
                    "monsterId", ambushingMonster.getId(),
                    "encounter", combatState
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", report.message()
        ));
    }

    @PostMapping("/long")
    public ResponseEntity<?> takeLongRest(@CurrentPlayerId Long playerId) {

        RestReport report = restingService.takeLongRest(playerId);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", report.message()
        ));
    }
}
