package com.java_dragons.dnd_tenebres.domain.quest.service;

import com.java_dragons.dnd_tenebres.core.event.ItemLootedEvent;
import com.java_dragons.dnd_tenebres.core.event.LocationClearedEvent;
import com.java_dragons.dnd_tenebres.core.event.MonsterKilledEvent;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.quest.entity.PlayerQuest;
import com.java_dragons.dnd_tenebres.domain.quest.entity.QuestTemplate;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestStatus;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestType;
import com.java_dragons.dnd_tenebres.domain.quest.repository.PlayerQuestRepository;
import com.java_dragons.dnd_tenebres.domain.quest.repository.QuestTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestService {

    private final PlayerQuestRepository playerQuestRepository;
    private final QuestTemplateRepository questTemplateRepository;
    private final PlayerRepository playerRepository;

    public PlayerQuest acceptQuest(Player player, QuestTemplate questTemplate) {
        if (player.getCurrentLocation() == null || !player.getCurrentLocation().getId().equals("city_adv_guild")) {
            throw new IllegalStateException("You cant accept a quest outside the Adventurers Guild");
        }
        if (player.isInCombat()) {
            throw new IllegalStateException("cant accept a quest while in combat");
        }
        if (playerQuestRepository.existsByPlayerIdAndQuestTemplateId(player.getId(), questTemplate.getId())) {
            throw new IllegalStateException("quest already exists");
        }

        PlayerQuest playerQuest = PlayerQuest.create(player, questTemplate);
        playerQuestRepository.save(playerQuest);
        return playerQuest;
    }

    public PlayerQuest acceptQuestById(Long playerId, Long questTemplateId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        QuestTemplate questTemplate = questTemplateRepository.findById(questTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Not found quest template"));

        return acceptQuest(player, questTemplate);
    }

    @EventListener
    public void onMonsterKilled(MonsterKilledEvent event) {
        List<PlayerQuest> quests = playerQuestRepository.findByPlayerIdAndQuestStatusAndQuestTemplateQuestTypeAndQuestTemplateTargetIdentifier(
                event.playerId(), QuestStatus.ACTIVE, QuestType.KILL_MONSTERS, event.monsterTemplateName());

        quests.forEach(playerQuest -> playerQuest.incrementProgress(1));
        playerQuestRepository.saveAll(quests);
    }

    @EventListener
    public void onItemLooted(ItemLootedEvent event) {
        List<PlayerQuest> quests = playerQuestRepository.findByPlayerIdAndQuestStatusAndQuestTemplateQuestTypeAndQuestTemplateTargetIdentifier(
                event.playerId(), QuestStatus.ACTIVE, QuestType.GATHER_ITEMS, event.itemTemplateName());

        quests.forEach(playerQuest -> playerQuest.incrementProgress(event.amount()));
        playerQuestRepository.saveAll(quests);
    }

    @EventListener
    public void onLocationCleared(LocationClearedEvent event) {
        List<PlayerQuest> quests = playerQuestRepository.findByPlayerIdAndQuestStatusAndQuestTemplateQuestTypeAndQuestTemplateTargetIdentifier(
                event.playerId(), QuestStatus.ACTIVE, QuestType.CLEAR_LOCATION, event.locationId());

        quests.forEach(playerQuest -> playerQuest.incrementProgress(1));
        playerQuestRepository.saveAll(quests);
    }

    public void turnInQuest(Long playerId, Long playerQuestId) {
        PlayerQuest playerQuest = playerQuestRepository.findByPlayerIdAndId(playerId, playerQuestId)
                .orElseThrow(() -> new IllegalArgumentException("Not found!"));

        Player player = playerQuest.getPlayer();

        player.addExperience(playerQuest.getRewardXp());
        player.addGold(playerQuest.getRewardGold());

        playerQuest.markAsRewarded();

        playerQuestRepository.save(playerQuest);
    }

    public List<QuestTemplate> getAvailableQuests(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (player.getCurrentLocation() == null ||
                !player.getCurrentLocation().getId().equals("city_adv_guild")) {
            throw new IllegalStateException("Player must be in Adventurers Guild");
        }

        return questTemplateRepository.findAvailableForPlayer(playerId);
    }

    public List<PlayerQuest> getActiveQuests(Long playerId) {
        return playerQuestRepository.findByPlayerIdAndQuestStatusIn(
                playerId,
                List.of(QuestStatus.ACTIVE, QuestStatus.COMPLETED)
        );
    }
}