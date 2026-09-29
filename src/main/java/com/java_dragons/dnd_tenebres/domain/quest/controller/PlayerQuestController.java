package com.java_dragons.dnd_tenebres.domain.quest.controller;

import com.java_dragons.dnd_tenebres.domain.quest.dto.*;
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
    public ResponseEntity<List<QuestTemplateResponse>> getAvailable(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(questService.getAvailableQuests(playerId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<PlayerQuestResponse>> getActive(@CurrentPlayerId Long playerId) {
        return ResponseEntity.ok(questService.getActiveQuests(playerId));
    }

    @PostMapping("/accept/{questTemplateId}")
    public ResponseEntity<PlayerQuestResponse> acceptQuest(
            @CurrentPlayerId Long playerId,
            @PathVariable Long questTemplateId) {

        return ResponseEntity.ok(questService.acceptQuestById(playerId, questTemplateId));
    }

    @PostMapping("/turn-in/{playerQuestId}")
    public ResponseEntity<QuestRewardResponse> turnIn(
            @CurrentPlayerId Long playerId,
            @PathVariable Long playerQuestId) {

        return ResponseEntity.ok(questService.turnInQuest(playerId, playerQuestId));
    }
}
