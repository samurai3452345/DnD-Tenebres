package com.java_dragons.dnd_tenebres.domain.exploration.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.exploration.dto.ExplorationReport;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
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
    private final LocationService locationService;
    private final InventoryService inventoryService;
    private final PlayerRepository playerRepository;

    @Transactional
    public ExplorationReport travel(Long playerId, String targetLocationId) {
        Player player = getPlayer(playerId);
        Location currentLocation = player.getCurrentLocation();

        // 1. Проверяем, доступны ли пути из текущей локации
        Set<Location> paths = locationService.getAvailableConnections(currentLocation.getId());

        Location targetLocation = paths.stream()
                .filter(loc -> loc.getId().equals(targetLocationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Вы не можете попасть туда отсюда!"));

        // 2. Двигаем игрока
        player.moveTo(targetLocation);
        playerRepository.save(player);

        log.info("Игрок {} перешел в локацию {}", player.getName(), targetLocation.getName());
        return ExplorationReport.moved(targetLocation.getName());
    }

    @Transactional
    public ExplorationReport hunt(Long playerId) {
        Player player = getPlayer(playerId);
        Location location = player.getCurrentLocation();

        if (location.getType() == LocationType.SAFE_ZONE) {
            return ExplorationReport.nothing("Здесь безопасно. Вы не нашли никого для охоты.");
        }

        if (location.getType() == LocationType.DANGEROUS) {
            return ExplorationReport.nothing("Вы победили всех врагов, здесь пусто.");
        }

        int roll = DiceRoller.rollD20();
        int wisMod = StatMathUtils.calculateModifier(player.getStats().getWisdom());
        int totalCheck = roll + wisMod;

        // В будущем сложность поиска (DC) можно привязать к локации
        int difficultyClass = 12;

        if (totalCheck >= difficultyClass) {
            // Спавним монстра в зависимости от БИОМА локации, убираем хардкод "green_forest"
            Monster monster = monsterSpawnerService.spawnRandomMonster(location.getId());

            log.info("Игрок {} нашел монстра: {}", player.getName(), monster.getName());
            return ExplorationReport.combat("Из теней появляется " + monster.getName() + "!", monster.getId());
        }

        return ExplorationReport.nothing("Вы долго бродили по окрестностям, но так никого и не выследили.");
    }

    @Transactional
    public ExplorationReport search(Long playerId) {
        Player player = getPlayer(playerId);
        Location location = player.getCurrentLocation();

        int roll = DiceRoller.rollD20();

        if (roll >= 15) { // 25% шанс найти случайный лут
            // Хардкод пока оставляем, так как у нас еще нет нормальной таблицы лута
            inventoryService.addItemToPlayer(player, "Кровоцвет", 1);
            return ExplorationReport.loot("Вам повезло! Вы нашли кое-что полезное.", List.of("Кровоцвет (1 шт.)"));
        }

        return ExplorationReport.nothing("Вы тщательно обыскали каждый угол, но нашли только пыль.");
    }

    private Player getPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));
    }
}