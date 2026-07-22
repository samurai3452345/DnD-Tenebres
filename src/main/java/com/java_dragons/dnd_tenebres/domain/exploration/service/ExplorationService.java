package com.java_dragons.dnd_tenebres.domain.exploration.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.model.CombatAction;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatService;
import com.java_dragons.dnd_tenebres.domain.exploration.model.ExplorationAction;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExplorationService {

    private final MonsterSpawnerService monsterSpawnerService;
    private final CombatService combatService;
    private final LocationService locationService;
    private final InventoryService inventoryService;

    @Transactional // Защищаем изменения состояния игрока и мира в БД
    public void explore(Player player, Location location, ExplorationAction action) {
        log.info("Вы выбрали действие: {}", action);

        switch (action) {
            case HUNT -> handleHunt(player, location);
            case SEARCH -> handleSearch(player, location);
            case TRAVEL -> handleTravel(player, location);
            default -> log.info("Вы стоите в раздумиях.");
        }
    }

    private void handleHunt(Player player, Location location) {
        String zoneId = location.getZoneId();

        if ("green_forest".equals(zoneId)) {
            log.info("Вы осматриваете заросли в поисках следов...");

            int roll = DiceRoller.rollD20();
            int wisMod = StatMathUtils.calculateModifier(player.getStats().getWisdom());
            int totalCheck = roll + wisMod;

            log.info("Бросок на Восприятие: {} + {} = {}", roll, wisMod, totalCheck);

            if (totalCheck >= 12) {
                log.info("Вы выследили врага! Бой начинается!");
                Monster monster = monsterSpawnerService.spawnRandomMonster("FOREST", location.getLevel());
                log.info("На вас нападает: {}", monster.getName());

                startCombatLoop(player, monster, 1);
            } else {
                log.info("Вы не нашли ни одного врага.");
            }

        } else if ("forgotten_crypt".equals(zoneId)) {
            log.info("Вы входите в комнату подземелья, враги уже ждут вас!");

            List<Monster> squad = monsterSpawnerService.spawnFixedMonstersForLocation(location.getId());

            if (squad.isEmpty()) {
                log.info("Здесь тихо... Врагов нет.");
                return;
            }

            log.info("Перед вами отряд из {} врагов!", squad.size());

            int aliveCount = squad.size();
            for (Monster m : squad) {
                if (player.getCurrentHp() <= 0) {
                    break;
                }
                startCombatLoop(player, m, aliveCount);
                aliveCount--;
            }
        } else {
            log.info("Здесь не на кого охотиться. Это безопасная зона.");
        }
    }

    private void handleSearch(Player player, Location location) {
        if ("crypt_armory".equals(location.getId())) {
            log.info("Вы обыскали пыльные стойки...");

            inventoryService.addItemToPlayer(player, "Ржавый меч", 1);
            inventoryService.addItemToPlayer(player, "Кровоцвет", 2);

            log.info("Найденные предметы успешно добавлены в ваш инвентарь!");
            // TODO: в будущем здесь нужно будет помечать в базе данных,
            // что игрок УЖЕ обыскал эту комнату, чтобы он не фармил мечи бесконечно.
        } else {
            log.info("Вы тщательно всё обыскали, но нашли только пыль и паутину.");
        }
    }

    private void handleTravel(Player player, Location location) {
        log.info("Вы находитесь в: {}", location.getName());
        log.info("Вы осматриваетесь в поисках путей...");

        Set<Location> paths = locationService.getAvailableConnections(location.getId());

        if (paths == null || paths.isEmpty()) {
            log.info("Тупик. Отсюда нет выхода.");
            return;
        }

        log.info("Доступные пути:");
        for (Location path : paths) {
            log.info("- {}", path.getName());
        }

        // TODO: автовыбор пути, потом поменять на выбор игрока
        Location nextLocation = paths.iterator().next();
        player.moveTo(nextLocation);
        log.info("🗺️ Вы отправились в: {}", nextLocation.getName());
    }

    private void startCombatLoop(Player player, Monster monster, int aliveEnemyCount) {
        log.info("⚔️ БОЙ НАЧИНАЕТСЯ: {} против {}!", player.getName(), monster.getName());

        int round = 1;
        while (player.getCurrentHp() > 0 && monster.getCurrentHp() > 0) {
            log.info("--- Раунд {} ---", round);

            // ✅ ИСПРАВЛЕНО: Вызываем executeTurn по новому контракту боевой системы.
            // Для консольного MVP эмулируем, что персонаж всегда атакует (CombatAction.ATTACK).
            // Если у персонажа в инвентаре будут зелья, здесь можно будет временно захардкодить
            // CombatAction.USE_POTION и передавать имя зелья для тестов.
            String roundLog = combatService.executeTurn(
                    player,
                    monster,
                    aliveEnemyCount,
                    round,
                    CombatAction.ATTACK,
                    null
            );

            log.info(roundLog);
            round++;
        }

        if (player.getCurrentHp() <= 0) {
            log.info("💀 {} пал в бою... Игра окончена.", player.getName());
        } else {
            log.info("🏆 {} повержен! Вы победили.", monster.getName());
            log.info("🎁 Вы обыскали врага...");

            inventoryService.addItemToPlayer(player, "Кровоцвет", 3);
            inventoryService.addItemToPlayer(player, "Ржавый меч", 1);
        }
    }
}