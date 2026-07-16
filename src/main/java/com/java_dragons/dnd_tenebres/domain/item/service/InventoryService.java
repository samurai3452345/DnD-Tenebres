package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.repository.ItemTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ItemTemplateRepository itemTemplateRepository;

    @Transactional
    public void addItemToPlayer(Player player, String templateName, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Количество добавляемых предметов должно быть больше нуля!");
        }

        ItemTemplate template = itemTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Справочник игры не содержит предмета с именем: " + templateName));

        // 2. Определяем поведение в зависимости от типа (Enum ItemType)
        if (template.getType() == ItemType.RESOURCE || template.getType() == ItemType.CONSUMABLE) {
            handleStackableItem(player, template, amount);
        } else {
            handleUniqueItem(player, template, amount);
        }
    }

    private void handleStackableItem(Player player, ItemTemplate template, int amount) {
        player.getInventory().stream()
                .filter(item -> item.getTemplate().getId().equals(template.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existingItem -> existingItem.setAmount(existingItem.getAmount() + amount),

                        () -> {
                            PlayerItem newItem = new PlayerItem();
                            newItem.setPlayer(player);
                            newItem.setTemplate(template);
                            newItem.setAmount(amount);
                            player.getInventory().add(newItem);
                        }
                );
    }

    private void handleUniqueItem(Player player, ItemTemplate template, int amount) {
        for (int i = 0; i < amount; i++) {
            PlayerItem newItem = new PlayerItem();
            newItem.setPlayer(player);
            newItem.setTemplate(template);
            newItem.setAmount(1);
            newItem.setEquipped(false);

            newItem.setBonusStrength(0);
            newItem.setBonusDexterity(0);
            newItem.setBonusConstitution(0);
            newItem.setBonusIntelligence(0);
            newItem.setBonusWisdom(0);
            newItem.setBonusCharisma(0);

            applyRandomStats(newItem, template.getStatBudget());
            player.getInventory().add(newItem);
        }
    }

    private void applyRandomStats(PlayerItem item, int budget) {
        if (budget <= 0) {
            return;
        }

        System.out.println("✨ Идет идентификация артефакта... Распределение " + budget + " очков статов:");

        for (int i = 0; i < budget; i++) {
            int randomStat = ThreadLocalRandom.current().nextInt(6);

            switch (randomStat) {
                case 0 -> item.setBonusStrength(item.getBonusStrength() + 1);
                case 1 -> item.setBonusDexterity(item.getBonusDexterity() + 1);
                case 2 -> item.setBonusConstitution(item.getBonusConstitution() + 1);
                case 3 -> item.setBonusIntelligence(item.getBonusIntelligence() + 1);
                case 4 -> item.setBonusWisdom(item.getBonusWisdom() + 1);
                case 5 -> item.setBonusCharisma(item.getBonusCharisma() + 1);
            }
        }


        System.out.println(String.format("   -> Сила: +%d | Ловкость: +%d | Телосложение: +%d | Интеллект: +%d | Мудрость: +%d | Харизма: +%d",
                item.getBonusStrength(), item.getBonusDexterity(), item.getBonusConstitution(),
                item.getBonusIntelligence(), item.getBonusWisdom(), item.getBonusCharisma()));
    }
}
