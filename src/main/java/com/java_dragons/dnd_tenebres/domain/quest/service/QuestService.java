package com.java_dragons.dnd_tenebres.domain.quest.service;

import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.core.event.*;
import com.java_dragons.dnd_tenebres.domain.item.repository.ItemTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerProgressionService;
import com.java_dragons.dnd_tenebres.domain.quest.dto.*;
import com.java_dragons.dnd_tenebres.domain.quest.entity.*;
import com.java_dragons.dnd_tenebres.domain.quest.model.*;
import com.java_dragons.dnd_tenebres.domain.quest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import java.util.List;
import com.java_dragons.dnd_tenebres.domain.economy.service.WalletService;
import com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason;

@Service
@RequiredArgsConstructor
public class QuestService {
    private final PlayerQuestRepository playerQuestRepository;
    private final QuestTemplateRepository questTemplateRepository;
    private final PlayerRepository playerRepository;
    private final PlayerProgressionService progressionService;
    private final InventoryService inventoryService;
    private final ItemTemplateRepository itemTemplateRepository;
    private final GameBalanceProperties balance;
    private final WalletService walletService;
    private final com.java_dragons.dnd_tenebres.infrastructure.audit.AuditService auditService;

    @Transactional
    public PlayerQuestResponse acceptQuestById(Long playerId, Long questTemplateId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));
        QuestTemplate template = questTemplateRepository.findById(questTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Quest template not found"));
        validateAcceptance(player, template);
        return map(playerQuestRepository.save(PlayerQuest.create(player, template)));
    }

    private void validateAcceptance(Player player, QuestTemplate template) {
        if (player.isInCombat()) throw new IllegalStateException("Нельзя принять квест во время боя");
        String requiredLocation = template.getAcceptLocationId() == null ? "city_adv_guild" : template.getAcceptLocationId();
        if (player.getCurrentLocation() == null || !requiredLocation.equals(player.getCurrentLocation().getId()))
            throw new IllegalStateException("Квест принимается в локации " + requiredLocation);
        if (player.getLevel() < template.getMinLevel())
            throw new IllegalStateException("Требуется уровень " + template.getMinLevel());
        if (playerQuestRepository.countByPlayerIdAndQuestStatusIn(player.getId(),
                List.of(QuestStatus.ACTIVE, QuestStatus.COMPLETED)) >= balance.maxActiveQuests())
            throw new IllegalStateException("Достигнут лимит активных заданий");
        if (template.getPrerequisiteQuestId() != null &&
                !playerQuestRepository.existsByPlayerIdAndQuestTemplateIdAndQuestStatus(player.getId(),
                        template.getPrerequisiteQuestId(), QuestStatus.REWARDED))
            throw new IllegalStateException("Не выполнено предыдущее задание");
        if (!template.isRepeatable() && playerQuestRepository.existsByPlayerIdAndQuestTemplateId(player.getId(), template.getId()))
            throw new IllegalStateException("Квест уже был принят");
        if (playerQuestRepository.existsByPlayerIdAndQuestTemplateIdAndQuestStatusIn(player.getId(), template.getId(),
                List.of(QuestStatus.ACTIVE, QuestStatus.COMPLETED)))
            throw new IllegalStateException("Квест уже активен");
    }

    @Transactional
    public QuestRewardResponse turnInQuest(Long playerId, Long playerQuestId) {
        PlayerQuest quest = playerQuestRepository.findByPlayerIdAndId(playerId, playerQuestId)
                .orElseThrow(() -> new IllegalArgumentException("Quest not found"));
        QuestTemplate template = quest.getQuestTemplate();
        if (quest.getQuestStatus() == QuestStatus.REWARDED)
            return new QuestRewardResponse(template.getRewardGold(),
                    new com.java_dragons.dnd_tenebres.domain.player.dto.LevelUpResult(0,
                            quest.getPlayer().getLevel(), quest.getPlayer().getLevel(), 0),
                    template.getRewardItemTemplateId(), template.getRewardItemAmount(), true);
        if (quest.getQuestStatus() != QuestStatus.COMPLETED)
            throw new IllegalStateException("Квест ещё не выполнен");
        if (!template.isRemoteTurnIn()) {
            String required = template.getTurnInLocationId() == null ? "city_adv_guild" : template.getTurnInLocationId();
            if (quest.getPlayer().getCurrentLocation() == null || !required.equals(quest.getPlayer().getCurrentLocation().getId()))
                throw new IllegalStateException("Квест сдаётся в локации " + required);
        }
        Player player = quest.getPlayer();
        var levelResult = progressionService.grantExperience(player, quest.getRewardXp());
        walletService.credit(player, quest.getRewardGold(), WalletReason.QUEST_REWARD, "QUEST", template.getId().toString());
        if (template.getRewardItemTemplateId() != null && template.getRewardItemAmount() > 0) {
            var item = itemTemplateRepository.findById(template.getRewardItemTemplateId())
                    .orElseThrow(() -> new IllegalStateException("Quest reward item is missing"));
            inventoryService.addItemToPlayer(player, item.getName(), template.getRewardItemAmount());
        }
        quest.markAsRewarded();
        auditService.record(player.getName(), playerId, "QUEST_REWARD", "SUCCESS", "quest=" + template.getId());
        return new QuestRewardResponse(quest.getRewardGold(), levelResult,
                template.getRewardItemTemplateId(), template.getRewardItemAmount(), false);
    }

    @Transactional(readOnly = true)
    public List<QuestTemplateResponse> getAvailableQuests(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
        String locationId = player.getCurrentLocation() == null ? null : player.getCurrentLocation().getId();
        return questTemplateRepository.findAvailableForPlayer(playerId).stream()
                .filter(q -> player.getLevel() >= q.getMinLevel())
                .filter(q -> q.getAcceptLocationId() == null ? "city_adv_guild".equals(locationId) : q.getAcceptLocationId().equals(locationId))
                .map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerQuestResponse> getActiveQuests(Long playerId) {
        return playerQuestRepository.findByPlayerIdAndQuestStatusIn(playerId,
                List.of(QuestStatus.ACTIVE, QuestStatus.COMPLETED)).stream().map(this::map).toList();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMonsterKilled(MonsterKilledEvent event) {
        progress(event.playerId(), QuestType.KILL_MONSTERS, event.monsterTemplateName(), 1);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onItemLooted(ItemLootedEvent event) {
        progress(event.playerId(), QuestType.GATHER_ITEMS, event.itemTemplateName(), event.amount());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLocationCleared(LocationClearedEvent event) {
        progress(event.playerId(), QuestType.CLEAR_LOCATION, event.locationId(), 1);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLocationVisited(LocationVisitedEvent event) {
        progress(event.playerId(), QuestType.VISIT_LOCATION, event.locationId(), 1);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onGenericProgress(QuestProgressEvent event) {
        progress(event.playerId(), event.type(), event.targetIdentifier(), event.amount());
    }

    private void progress(Long playerId, QuestType type, String target, int amount) {
        List<PlayerQuest> quests = playerQuestRepository
                .findByPlayerIdAndQuestStatusAndQuestTemplateQuestTypeAndQuestTemplateTargetIdentifier(
                        playerId, QuestStatus.ACTIVE, type, target);
        quests.forEach(quest -> quest.incrementProgress(amount));
        playerQuestRepository.saveAll(quests);
    }

    private PlayerQuestResponse map(PlayerQuest quest) {
        return new PlayerQuestResponse(quest.getId(), map(quest.getQuestTemplate()), quest.getCurrentProgress(),
                quest.getQuestTemplate().getTargetCount(), quest.getQuestStatus().name(),
                quest.getQuestStatus() == QuestStatus.COMPLETED);
    }

    private QuestTemplateResponse map(QuestTemplate q) {
        return new QuestTemplateResponse(q.getId(), q.getName(), q.getDescription(), q.getQuestType().name(),
                (q.getQuestSource() == null ? QuestSource.GUILD : q.getQuestSource()).name(),
                q.getTargetIdentifier(), q.getTargetCount(), q.getRewardXp(), q.getRewardGold(), q.getMinLevel(),
                q.getAcceptLocationId(), q.getTurnInLocationId(), q.getPrerequisiteQuestId(), q.isRepeatable(),
                q.getRewardItemTemplateId(), q.getRewardItemAmount());
    }
}
