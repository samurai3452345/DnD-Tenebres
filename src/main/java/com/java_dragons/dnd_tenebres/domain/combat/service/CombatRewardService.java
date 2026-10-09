package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.event.ItemLootedEvent;
import com.java_dragons.dnd_tenebres.core.event.LocationClearedEvent;
import com.java_dragons.dnd_tenebres.core.event.MonsterKilledEvent;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason;
import com.java_dragons.dnd_tenebres.domain.economy.service.WalletService;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationClearService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.monster.service.LootGeneratorService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CombatRewardService {
    private final PlayerService playerService;
    private final WalletService walletService;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final LootGeneratorService lootGeneratorService;
    private final InventoryService inventoryService;
    private final LocationClearService locationClearService;
    private final ApplicationEventPublisher eventPublisher;

    public void grant(Player player, Monster monster, int aliveEnemyCount, List<CombatEvent> events) {
        eventPublisher.publishEvent(new MonsterKilledEvent(player.getId(), monster.getTemplateName()));
        playerService.addExperienceToPlayer(player, monster.getXpReward());
        if (monster.getGoldReward() > 0) {
            walletService.credit(player, monster.getGoldReward(), WalletReason.COMBAT_REWARD,
                    "MONSTER", monster.getId().toString());
        }
        events.add(new CombatEvent("Система", "REWARD", player.getName(), monster.getXpReward(), "Получен опыт"));
        if (monster.getGoldReward() > 0) {
            events.add(new CombatEvent("Система", "REWARD", player.getName(), monster.getGoldReward(), "Найдено золото"));
        }

        var template = monsterTemplateRepository.findByName(monster.getTemplateName())
                .orElseThrow(() -> new IllegalStateException("Шаблон монстра не найден: " + monster.getTemplateName()));
        var loot = lootGeneratorService.generateLootForMonster(template);
        if (loot.isEmpty()) {
            events.add(new CombatEvent(monster.getName(), "LOOT", player.getName(), 0,
                    "Монстр не оставил после себя ничего ценного."));
        } else {
            loot.forEach((item, amount) -> {
                inventoryService.addItemToPlayer(player, item.getName(), amount);
                eventPublisher.publishEvent(new ItemLootedEvent(player.getId(), item.getName(), amount));
                events.add(new CombatEvent(monster.getName(), "LOOT", player.getName(), amount,
                        "Выбито: " + item.getName() + " (x" + amount + ")"));
            });
        }

        if (aliveEnemyCount <= 1 && player.getCurrentLocation() != null) {
            String locationId = player.getCurrentLocation().getId();
            if (locationClearService.markLocationAsCleared(player.getId(), locationId)) {
                eventPublisher.publishEvent(new LocationClearedEvent(player.getId(), locationId));
                events.add(new CombatEvent("Система", "ROOM_CLEARED", player.getName(), 0,
                        "Врагов больше нет. Локация зачищена!"));
            }
        }
    }
}
