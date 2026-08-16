package com.java_dragons.dnd_tenebres.domain.quest.controller;

import com.java_dragons.dnd_tenebres.domain.quest.entity.PlayerQuest;
import com.java_dragons.dnd_tenebres.domain.quest.entity.QuestTemplate;
import com.java_dragons.dnd_tenebres.domain.quest.service.QuestService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
public class PlayerQuestController {

    private final QuestService questService;

    @GetMapping("/available")
    public ResponseEntity<List<QuestTemplate>> getAvailable(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(questService.getAvailableQuests(playerId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<PlayerQuest>> getActive(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(questService.getActiveQuests(playerId));
    }

    @PostMapping("/accept/{questTemplateId}")
    public ResponseEntity<PlayerQuest> acceptQuest(
            @CurrentPlayerId Long playerId,
            @PathVariable Long questTemplateId) {

        PlayerQuest quest = questService.acceptQuestById(playerId, questTemplateId);
        return ResponseEntity.ok(quest);
    }

    @PostMapping("/turn-in/{playerQuestId}")
    public ResponseEntity<?> turnIn(
            @CurrentPlayerId Long playerId,
            @PathVariable Long playerQuestId) {

        questService.turnInQuest(playerId, playerQuestId);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Квест успешно сдан, награда получена!"
        ));
    }
}