package com.java_dragons.dnd_tenebres.runner;

import com.java_dragons.dnd_tenebres.domain.exploration.model.ExplorationAction;
import com.java_dragons.dnd_tenebres.domain.exploration.service.ExplorationService;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.item.service.EquipmentService;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GameSimulationRunner implements CommandLineRunner {

    private final ExplorationService explorationService;
    private final LocationService locationService;
    private final InventoryService inventoryService;
    private final PlayerRepository playerRepository;
    private final EquipmentService equipmentService;


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println("🔥 ТЕСТ СИСТЕМЫ ИНВЕНТАРЯ 🔥");
        System.out.println("=================================================\n");

        playerRepository.deleteAll();
        playerRepository.flush();

        com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats alaricStats =
                new com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats(16, 14, 15, 10, 10, 10);

        Player player = Player.builder()
                .name("Артур Магомедов")
                .level(1)
                .maxHp(100)
                .currentHp(100)
                .gold(50)
                .experience(0)
                .stats(alaricStats)
                .build();

        player = playerRepository.save(player);
        System.out.println(" Герой " + player.getName() + " создан и сохранен в БД.");

        try {
            System.out.println("\n Боги посылают Артуру дары...");

            inventoryService.addItemToPlayer(player, "Кровоцвет", 3);
            inventoryService.addItemToPlayer(player, "Кровоцвет", 2); // Должно стать 5

            inventoryService.addItemToPlayer(player, "Кольцо Всевластия", 1);

            inventoryService.addItemToPlayer(player, "Ржавый меч", 1);

            player = playerRepository.save(player);

            System.out.println("\n ЗАГЛЯДЫВАЕМ В РЮКЗАК АРТУРА:");
            for (PlayerItem item : player.getInventory()) {
                System.out.print("- " + item.getTemplate().getName() + " (Количество: " + item.getAmount() + ")");

                if (item.getTemplate().getStatBudget() > 0) {
                    System.out.println("\n    [Артефакт опознан! СИЛ+" + item.getBonusStrength() +
                            " | ЛОВ+" + item.getBonusDexterity() +
                            " | ТЕЛ+" + item.getBonusConstitution() +
                            " | ИНТ+" + item.getBonusIntelligence() +
                            " | МУД+" + item.getBonusWisdom() +
                            " | ХАР+" + item.getBonusCharisma() + "]");
                } else {
                    System.out.println();
                }
            }


            System.out.println("\n АРТУР ПРИМЕРЯЕТ ЭКИПИРОВКУ:");

            PlayerItem sword = player.getInventory().stream()
                    .filter(item -> item.getTemplate().getName().equals("Ржавый меч"))
                    .findFirst()
                    .orElseThrow();

            PlayerItem ring = player.getInventory().stream()
                    .filter(item -> item.getTemplate().getName().equals("Кольцо Всевластия"))
                    .findFirst()
                    .orElseThrow();

            equipmentService.equipItem(player, sword.getId(), EquipmentSlot.MAIN_HAND);
            equipmentService.equipItem(player, ring.getId(), EquipmentSlot.RING_1);

            player = playerRepository.save(player);
            System.out.println("✅ Экипировка успешно надета и сохранена в БД!");


            System.out.println("\n--- 🎬 СЦЕНА 1: ВХОД В СКЛЕП ---");
            Location startLocation = locationService.getLocationById("crypt_entrance"); // Убедись что ID локации совпадает с твоим
            player.moveTo(startLocation);
            explorationService.explore(player, startLocation, ExplorationAction.HUNT);

        } catch (Exception e) {
            System.err.println("\n❌ Симуляция прервана: " + e.getMessage());
        }
    }
}