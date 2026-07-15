package com.java_dragons.dnd_tenebres.runner;

import com.java_dragons.dnd_tenebres.domain.exploration.model.ExplorationAction;
import com.java_dragons.dnd_tenebres.domain.exploration.service.ExplorationService;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameSimulationRunner implements CommandLineRunner {

    private final ExplorationService explorationService;
    private final LocationService locationService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");

        PlayerStats alaricStats = new PlayerStats(16, 14, 15, 10, 10, 10);

        Player player = Player.builder()
                .name("Аларик Роуэн")
                .level(1)
                .maxHp(100)
                .currentHp(100)
                .gold(50)
                .experience(0)
                .stats(alaricStats)
                .build();

        System.out.println("Герой " + player.getName() + " готов к бою! (HP: " + player.getCurrentHp() + ")");

        try {
            Location startLocation = locationService.getLocationById("crypt_entrance");
            player.moveTo(startLocation);

            System.out.println("\n---  СЦЕНА 1: ВХОД В СКЛЕП ---");
            explorationService.explore(player, player.getCurrentLocation(), ExplorationAction.HUNT);

            if (player.getCurrentHp() <= 0) {
                System.out.println(" Аларик погиб слишком рано. Симуляция прервана.");
                return;
            }

            System.out.println("\n---  СЦЕНА 2: ПРОДВИЖЕНИЕ ВГЛУБЬ ---");
            explorationService.explore(player, player.getCurrentLocation(), ExplorationAction.TRAVEL);

            System.out.println("\n---  СЦЕНА 3: НОВАЯ БИТВА ---");
            explorationService.explore(player, player.getCurrentLocation(), ExplorationAction.HUNT);

            System.out.println("\n=================================================");
            System.out.println(" СИМУЛЯЦИЯ УСПЕШНО ЗАВЕРШЕНА ");
            System.out.println("Остаток ХП героя: " + player.getCurrentHp());
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("❌ Ошибка во время симуляции: " + e.getMessage());
            e.printStackTrace();
        }
    }
}