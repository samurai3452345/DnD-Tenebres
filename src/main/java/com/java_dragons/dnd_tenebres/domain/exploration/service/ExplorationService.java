package com.java_dragons.dnd_tenebres.domain.exploration.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatService;
import com.java_dragons.dnd_tenebres.domain.exploration.model.ExplorationActiom;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExplorationService {
    private final MonsterSpawnerService monsterSpawnerService;
    private final CombatService combatService;

    public void explore(Player player, Location location, ExplorationActiom actiom) {
        System.out.println("\n Вы выбрали: " + actiom);

        switch (actiom){
            case HANT -> handleHunt(player, location);
            case SEARCH -> handleSearch(player, location);
            case TRAVEL -> handleTravel(player, location);
            default -> System.out.println("Вы стоите в раздумиях.");
        }
    }
    private void handleHunt(Player player, Location location) {
        String zoneId = location.getZoneId();

        if ("green_forest".equals(zoneId)) {
            System.out.println("Вы осматриваете заросли в поисках следов...");

            int roll = DiceRoller.rollD20();
            int wisMod = StatMathUtils.calculateModifier(player.getStats().getWisdom());
            int totalCheck = roll + wisMod;

            System.out.println("Бросок на Восприятие: " + roll + " + " + wisMod + " = " + totalCheck);

            if (totalCheck >= 12) {
                System.out.println("Вы выследили врага! Бой начинается!");
                Monster monster = monsterSpawnerService.spawnRandomMonster("FOREST", location.getLevel());
                System.out.println("На вас нападает: " + monster.getName());

                // TODO: Вызов combatService.executeRound(...) в цикле
            } else {
                System.out.println("Вы не нашли ни одноги врага");
            }

        } else if ("forgotten_crypt".equals(zoneId)) {
            System.out.println("Вы входите в комнату подземелья, враги уже ждут вас!");

            // TODO: Вызвать метод спавна фиксированных врагов (напишем его позже)
            // List<Monster> squad = monsterSpawnerService.spawnFixedMonstersForLocation(location.getId());
            // System.out.println("Перед вами отряд из " + squad.size() + " врагов!");

        } else {
            System.out.println("Здесь не на кого охотиться. Это безопасная зона.");
        }
    }

    private void handleSearch(Player player, Location location) {
        if ("crypt_armory".equals(location.getId())) {
            System.out.println("⚔️ Вы обыскали пыльные стойки и нашли Оружие!");
            // TODO: Сгенерировать предмет и положить в инвентарь (Следующий этап)
        } else {
            System.out.println(" Вы тщательно всё обыскали, но нашли только пыль и паутину.");
        }
    }

    private void handleTravel(Player player, Location location) {
        // TODO: Логика вывода доступных дорог и перемещения по графу
        System.out.println(" Вы изучаете карту, чтобы выбрать следующий путь...");
    }
}
