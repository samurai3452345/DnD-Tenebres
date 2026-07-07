package com.java_dragons.dnd_tenebres.runner;

import com.java_dragons.dnd_tenebres.domain.combat.service.CombatService;
import com.java_dragons.dnd_tenebres.domain.item.entity.Weapon;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemRarity;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.service.MonsterSpawnerService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameSimulationRunner implements CommandLineRunner {

    private final MonsterSpawnerService monsterSpawnerService;
    private final CombatService combatService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== ⚔️ СТАРТ СИМУЛЯЦИИ ⚔️ ===");

        PlayerStats stats = PlayerStats.builder()
                .strength(16)
                .dexterity(14)
                .constitution(14)
                .intelligence(10)
                .wisdom(10)
                .charisma(10)
                .build();

        Player player = Player.builder()
                .name("Артур")
                .level(1)
                .experience(0L)
                .gold(100L)
                .currentHp(14)
                .stats(stats)
                .build();

        Weapon sword = new Weapon();
        sword.setName("Железный Длинный Меч");
        sword.setRarity(ItemRarity.UNCOMMON);
        sword.setBaseDiceType(DiceType.D8);

        Weapon dagger = new Weapon();
        dagger.setName("Ржавый кинжал");
        dagger.setRarity(ItemRarity.COMMON);
        dagger.setBaseDiceType(DiceType.D4);


        Monster monster = monsterSpawnerService.spawnRandomMonster(1);
        System.out.println("На пути появляется: " + monster.getName() +
                " [ХП: " + monster.getCurrentHp() +
                ", Броня (AC): " + monster.getArmorClass() + "]");

        System.out.println("--- БОЙ НАЧИНАЕТСЯ ---\n");

        int round = 1;
        while (monster.getCurrentHp() > 0) {
            System.out.println("====== Раунд " + round + " ======");

            String attackLog = combatService.executeAttack(player, sword, monster);
            System.out.println(attackLog);



            round++;

            Thread.sleep(1500);
        }

        System.out.println("\n=== 🏆 СИМУЛЯЦИЯ ОКОНЧЕНА. 🏆 ===");
    }
}