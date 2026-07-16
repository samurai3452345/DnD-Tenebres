package com.java_dragons.dnd_tenebres.domain.exploration.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.core.math.StatMathUtils;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatService;
import com.java_dragons.dnd_tenebres.domain.exploration.model.ExplorationAction;
import com.java_dragons.dnd_tenebres.domain.item.entity.Weapon;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExplorationService {
    private final MonsterSpawnerService monsterSpawnerService;
    private final CombatService combatService;
    private final LocationService locationService;
    private final InventoryService inventoryService;
    private final InventoryService inventoryService2;


    public void explore(Player player, Location location, ExplorationAction actiom) {
        System.out.println("\n Вы выбрали: " + actiom);

        switch (actiom){
            case HUNT -> handleHunt(player, location);
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


                Weapon tempWeapon = new Weapon();
                startCombatLoop(player, tempWeapon, monster);            } else {
                System.out.println("Вы не нашли ни одного врага");
            }

        } else if ("forgotten_crypt".equals(zoneId)) {
            System.out.println("Вы входите в комнату подземелья, враги уже ждут вас!");

             List<Monster> squad = monsterSpawnerService.spawnFixedMonstersForLocation(location.getId());
             System.out.println("Перед вами отряд из " + squad.size() + " врагов!");

            if (squad.isEmpty()) {
                System.out.println("Здесь тихо... Врагов нет.");
                return;
            }

            System.out.println("Перед вами отряд из " + squad.size() + " врагов!");
            Weapon weapon = new Weapon();

            for (Monster m : squad) {
                if (player.getCurrentHp() <= 0) {
                    break;
                }

                startCombatLoop(player, weapon, m);
            }
        } else {
            System.out.println("Здесь не на кого охотиться. Это безопасная зона.");
        }
    }

    private void handleSearch(Player player, Location location) {
        if ("crypt_armory".equals(location.getId())) {
            System.out.println(" Вы обыскали пыльные стойки...");

            inventoryService.addItemToPlayer(player, "Ржавый меч", 1);
            inventoryService.addItemToPlayer(player, "Кровоцвет", 2);

            System.out.println("Найденные предметы успешно добавлены в ваш инвентарь!");

            // TODO: в будущем здесь нужно будет помечать в базе данных,
            // что игрок УЖЕ обыскал эту комнату, чтобы он не фармил мечи бесконечно.

        } else {
            System.out.println(" Вы тщательно всё обыскали, но нашли только пыль и паутину.");
        }
    }

    private void handleTravel(Player player, Location location) {
        System.out.println("Вы находитесь в: " + location.getName());
        System.out.println("Вы осматриваетесь в поисках путей...");

         Set<Location> paths = locationService.getAvailableConnections(location.getId());

        if (paths == null || paths.isEmpty()) {
            System.out.println("Тупик. от сюда нет выхода. ");
            return;
        }

        System.out.println("Доступные пути: ");
        for (Location path : paths) {
            System.out.println("- " + path.getName());
        }

        // Авто выбор пути патом надо поменять
        Location nextLocation = paths.iterator().next();
        player.moveTo(nextLocation);
        System.out.println("🗺️ Вы отправились в: " + nextLocation.getName());
    }

    private void startCombatLoop(Player player, Weapon weapon, Monster monster) {
        System.out.println("\n⚔️ БОЙ НАЧИНАЕТСЯ: " + player.getName() + " против " + monster.getName() + "!");

        int round = 1;
        while (player.getCurrentHp() > 0 && monster.getCurrentHp() > 0) {
            System.out.println("--- Раунд " + round + " ---");

            String roundLog = combatService.executeRound(player, monster, round);
            System.out.println(roundLog);

            round++;
        }

        if (player.getCurrentHp() <= 0) {
            System.out.println("💀 " + player.getName() + " пал в бою... Игра окончена.");
        } else {
            System.out.println("🏆 " + monster.getName() + " повержен! Вы победили.");

            System.out.println("🎁 Вы обыскали врага...");
            inventoryService.addItemToPlayer(player, "Кровоцвет", 3);
            inventoryService.addItemToPlayer(player, "Ржавый меч", 1);
        }
    }
}
