package com.java_dragons.dnd_tenebres.domain.quest.controller;


import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.quest.entity.PlayerQuest;
import com.java_dragons.dnd_tenebres.domain.quest.entity.QuestTemplate;
import com.java_dragons.dnd_tenebres.domain.quest.service.QuestService;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*

@PostMapping("/hunt")
    public ResponseEntity<ExplorationReport> hunt(@CurrentPlayerId Long playerId) {
        ExplorationReport report = explorationService.hunt(playerId);
        return ResponseEntity.ok(report);
    }


POST /{playerId}/accept/{questTemplateId}
 */
@RestController
@RequestMapping("/api/v1/quests")
@RequiredArgsConstructor
public class PlayerQuestController {

    private final QuestService questService;

    @PostMapping("/accept/{questTemplateId}")
    public ResponseEntity<PlayerQuest> getPlayerQuest(@CurrentPlayerId Long playerId ,@PathVariable Long questTemplateId) {
         PlayerQuest quest = questService.acceptQuestById(playerId,questTemplateId);

        return ResponseEntity.ok(quest);
    }
    @GetMapping("/available")
    public List<QuestTemplate> getAvailable(@CurrentPlayerId Long playerId) {
        return questService.getAvailableQuests(playerId);
    }
    @GetMapping("/active")
    public List<PlayerQuest> getActive(@CurrentPlayerId Long playerId) {
        return questService.getActiveQuests(playerId);
    }
    @PostMapping("/turn-in/{playerQuestId}")
    public void turnIn(@CurrentPlayerId Long playerId, @PathVariable Long playerQuestId) {
        questService.turnInQuest(playerId, playerQuestId);
    }

}
