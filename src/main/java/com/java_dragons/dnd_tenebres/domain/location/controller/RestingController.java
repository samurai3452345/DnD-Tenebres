package com.java_dragons.dnd_tenebres.domain.location.controller;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatReport;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatService;
import com.java_dragons.dnd_tenebres.domain.location.service.RestingService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.dto.RestReport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/rest")
@RequiredArgsConstructor
public class RestingController {

    private final RestingService restingService;
    private final CombatService combatService;
    private final MonsterSpawnerService monsterSpawnerService;

    @PostMapping("/short/{playerId}")
    public ResponseEntity<?> takeShortRest(@PathVariable Long playerId) {

        RestReport report = restingService.takeShortRest(playerId);

        if (report.isAmbushed()) {

            Monster ambushingMonster = monsterSpawnerService.spawnRandomMonster(report.biome(), report.level());

            CombatReport ambushReport = combatService.executeAmbushTurn(playerId, ambushingMonster);

            return ResponseEntity.ok(Map.of(
                    "status", "AMBUSH",
                    "message", report.message(),
                    "monster", ambushingMonster.getName(),
                    "combatLog", ambushReport
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", report.message()
        ));
    }

    @PostMapping("/long/{playerId}")
    public ResponseEntity<?> takeLongRest(@PathVariable Long playerId) {

        RestReport report = restingService.takeLongRest(playerId);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", report.message()
        ));
    }
}