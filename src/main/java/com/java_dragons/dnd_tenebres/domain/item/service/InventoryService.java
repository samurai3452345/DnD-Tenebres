package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.model.MagicWeaponEffect;
import com.java_dragons.dnd_tenebres.domain.item.repository.ItemTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ItemTemplateRepository itemTemplateRepository;
    private static final int MAX_RESOURCE_STACK = 100;
    private static final int MAX_CONSUMABLE_STACK = 16;

    @Transactional
    public void addItemToPlayer(Player player, String templateName, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Количество добавляемых предметов должно быть больше нуля!");
        }

        ItemTemplate template = itemTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Справочник игры не содержит предмета с именем: " + templateName));

        if (template.getType() == ItemType.RESOURCE || template.getType() == ItemType.CONSUMABLE) {
            handleStackableItem(player, template, amount);
        } else {
            handleUniqueItem(player, template, amount);
        }
    }

    private void handleStackableItem(Player player, ItemTemplate template, int amount) {
        int maxStackSize = (template.getType() == ItemType.RESOURCE) ? MAX_RESOURCE_STACK : MAX_CONSUMABLE_STACK;

        // Ищем все неполные стаки ОДИН раз
        List<PlayerItem> incompleteStacks = player.getInventory().stream()
                .filter(item -> item.getTemplate().getId().equals(template.getId()))
                .filter(item -> item.getAmount() < maxStackSize)
                .toList();

        int remainingAmount = amount;

        for (PlayerItem existingItem : incompleteStacks) {
            if (remainingAmount <= 0) break;

            int spaceLeft = maxStackSize - existingItem.getAmount();
            int toAdd = Math.min(remainingAmount, spaceLeft);

            existingItem.setAmount(existingItem.getAmount() + toAdd);
            remainingAmount -= toAdd;
        }

        // Если остались предметы, создаем новые полные/частичные стаки
        while (remainingAmount > 0) {
            int toAdd = Math.min(remainingAmount, maxStackSize);
            player.getInventory().add(createBaseItem(player, template, toAdd));
            remainingAmount -= toAdd;
        }
    }

    private void handleUniqueItem(Player player, ItemTemplate template, int amount) {
        for (int i = 0; i < amount; i++) {
            PlayerItem newItem = createBaseItem(player, template, 1);
            applyRandomStats(newItem, template.getStatBudget());

            if (template.getType() == ItemType.MAGIC_WEAPON) {
                rollMagicWeaponEffect(newItem);
            }

            player.getInventory().add(newItem);
        }
    }

    private void rollMagicWeaponEffect(PlayerItem item) {
        MagicWeaponEffect[] effects = MagicWeaponEffect.values();
        int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, effects.length);
        MagicWeaponEffect rolledEffect = effects[randomIndex];

        item.setMagicEffect(rolledEffect);

        if (rolledEffect == MagicWeaponEffect.ELEMENTAL_MASTERY) {
            DamageType[] elements = DamageType.values();
            int randomElement = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, elements.length);
            item.setMagicEffectElement(elements[randomElement]);
        }
    }

    private PlayerItem createBaseItem(Player player, ItemTemplate template, int amount) {
        PlayerItem item = new PlayerItem();
        item.setPlayer(player);
        item.setTemplate(template);
        item.setAmount(amount);
        item.setEquipped(false);
        item.setBonusStrength(0);
        item.setBonusDexterity(0);
        item.setBonusConstitution(0);
        item.setBonusIntelligence(0);
        item.setBonusWisdom(0);
        item.setBonusCharisma(0);
        return item;
    }

    private void applyRandomStats(PlayerItem item, int budget) {
        if (budget <= 0) {
            return;
        }

        log.debug("✨ Идет идентификация артефакта... Распределение {} очков статов", budget);

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

        log.debug("   -> Сила: +{} | Ловкость: +{} | Телосложение: +{} | Интеллект: +{} | Мудрость: +{} | Харизма: +{}",
                item.getBonusStrength(), item.getBonusDexterity(), item.getBonusConstitution(),
                item.getBonusIntelligence(), item.getBonusWisdom(), item.getBonusCharisma());
    }
}