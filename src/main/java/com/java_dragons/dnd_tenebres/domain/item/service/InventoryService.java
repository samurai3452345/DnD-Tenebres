package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.EquipmentSlot;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.model.MagicWeaponEffect;
import com.java_dragons.dnd_tenebres.domain.item.repository.ItemTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.item.repository.PlayerItemRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.*;
import com.java_dragons.dnd_tenebres.core.random.RandomSource;
import com.java_dragons.dnd_tenebres.domain.item.dto.*;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.core.event.QuestProgressEvent;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestType;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ItemTemplateRepository itemTemplateRepository;
    private final PlayerRepository playerRepository;
    private final PlayerItemRepository playerItemRepository;
    private final PotionService potionService;
    private final RandomSource randomSource;
    private final ApplicationEventPublisher eventPublisher;

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
        int randomIndex = randomSource.nextInt(1, effects.length);
        MagicWeaponEffect rolledEffect = effects[randomIndex];

        item.setMagicEffect(rolledEffect);

        if (rolledEffect == MagicWeaponEffect.ELEMENTAL_MASTERY) {
            DamageType[] elements = DamageType.values();
            int randomElement = randomSource.nextInt(1, elements.length);
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
        item.setDurability(template.getMaxDurability());
        return item;
    }

    private void applyRandomStats(PlayerItem item, int budget) {
        if (budget <= 0) {
            return;
        }

        log.debug("✨ Идет идентификация артефакта... Распределение {} очков статов", budget);

        for (int i = 0; i < budget; i++) {
            int randomStat = randomSource.nextInt(0, 6);
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
    @Transactional(readOnly = true)
    public List<PlayerItem> getPlayerInventory(Long playerId) {
        return playerItemRepository.findByPlayerId(playerId);
    }

    @Transactional(readOnly = true)
    public com.java_dragons.dnd_tenebres.domain.item.dto.InventoryResponse getInventoryResponse(Long playerId) {
        List<com.java_dragons.dnd_tenebres.domain.item.dto.InventoryItemResponse> items =
                playerItemRepository.findByPlayerId(playerId).stream().map(this::toResponse).toList();
        List<EquipmentResponse> equipment = items.stream().filter(InventoryItemResponse::equipped)
                .map(item -> new EquipmentResponse(item.equippedSlot(), item)).toList();
        Map<String, Long> sets = items.stream().filter(InventoryItemResponse::equipped)
                .filter(item -> item.template().setCode() != null)
                .collect(java.util.stream.Collectors.groupingBy(item -> item.template().setCode(), java.util.stream.Collectors.counting()));
        List<InventoryResponse.ItemSetBonusResponse> bonuses = sets.entrySet().stream().map(entry -> {
            int pieces = entry.getValue().intValue();
            List<String> active = new ArrayList<>();
            if (pieces >= 2) active.add("TWO_PIECE_BONUS");
            if (pieces >= 3) active.add("THREE_PIECE_BONUS");
            if (pieces >= 4) active.add("FULL_SET_BONUS");
            return new InventoryResponse.ItemSetBonusResponse(entry.getKey(), pieces, active);
        }).toList();
        return new InventoryResponse(items, items.size(), equipment, bonuses);
    }

    @Transactional(readOnly = true)
    public com.java_dragons.dnd_tenebres.domain.item.dto.InventoryItemResponse getItemResponse(Long playerId, Long itemId) {
        return toResponse(requireOwnedItem(playerId, itemId));
    }

    @Transactional
    public void setLocked(Long playerId, Long itemId, boolean locked) {
        requireOwnedItem(playerId, itemId).setLocked(locked);
    }

    private PlayerItem requireOwnedItem(Long playerId, Long itemId) {
        return playerItemRepository.findByIdAndPlayerId(itemId, playerId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден или не принадлежит игроку"));
    }

    private com.java_dragons.dnd_tenebres.domain.item.dto.InventoryItemResponse toResponse(PlayerItem item) {
        ItemTemplate t = item.getTemplate();
        var template = new com.java_dragons.dnd_tenebres.domain.item.dto.ItemTemplateResponse(
                t.getId(), t.getName(), t.getType(), t.getSlot(), t.getRarity(), t.getArmorType(),
                t.getArmorClass(), t.getRequiredStrength(), t.getRequiredLevel(), t.getMaxDurability(), t.getSetCode(),
                t.getDamageDice(), t.getDiceCount(),
                t.getPassiveEffect(), t.getConsumableAction());
        var bonuses = new com.java_dragons.dnd_tenebres.domain.item.dto.InventoryItemResponse.ItemBonuses(
                item.getBonusStrength(), item.getBonusDexterity(), item.getBonusConstitution(),
                item.getBonusIntelligence(), item.getBonusWisdom(), item.getBonusCharisma());
        return new com.java_dragons.dnd_tenebres.domain.item.dto.InventoryItemResponse(
                item.getId(), template, item.getAmount(), item.isEquipped(), item.getEquippedSlot(),
                item.isLocked(), item.getTier(), item.getItemXp(), bonuses,
                item.getMagicEffect(), item.getMagicEffectElement(), item.getDurability(), t.getMaxDurability());
    }

    @Transactional
    public void equipItem(Long playerId, Long itemId, EquipmentSlot slot) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        PlayerItem itemToEquip = playerItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));

        if (!itemToEquip.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException("Это не ваш предмет!");
        }

        if (player.getLevel() < itemToEquip.getTemplate().getRequiredLevel())
            throw new IllegalStateException("Требуется уровень " + itemToEquip.getTemplate().getRequiredLevel());
        if (itemToEquip.getDurability() <= 0) throw new IllegalStateException("Предмет сломан");
        if (player.isInCombat()) throw new IllegalStateException("Нельзя менять экипировку во время боя");

        player.equipItem(itemId, slot);
    }

    @Transactional
    public void unequipItem(Long playerId, EquipmentSlot slot) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        player.unequipItem(slot);
    }

    @Transactional
    public UseItemResponse useItem(Long playerId, Long itemId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));
        if (player.isInCombat()) throw new IllegalStateException("В бою используйте действие USE_POTION");
        PlayerItem item = requireOwnedItem(playerId, itemId);
        if (item.getTemplate().getType() != ItemType.CONSUMABLE || item.getAmount() <= 0)
            throw new IllegalArgumentException("Предмет нельзя использовать");
        List<CombatEvent> events = new ArrayList<>();
        if (!potionService.applyPotion(item.getTemplate(), player, events))
            throw new IllegalStateException("У предмета нет применимого эффекта");
        player.consumeItem(item);
        int remaining = Math.max(0, item.getAmount());
        eventPublisher.publishEvent(new QuestProgressEvent(playerId, QuestType.USE_ITEM,
                item.getTemplate().getName(), 1));
        return new UseItemResponse(itemId, true, remaining, player.getCurrentHp(), player.getCurrentMp(), events);
    }

    @Transactional(readOnly = true)
    public ItemComparisonResponse compare(Long playerId, Long itemId) {
        PlayerItem candidate = requireOwnedItem(playerId, itemId);
        EquipmentSlot slot = candidate.getTemplate().getSlot();
        PlayerItem equipped = playerItemRepository.findByPlayerId(playerId).stream()
                .filter(PlayerItem::isEquipped)
                .filter(item -> item.getEquippedSlot() == slot ||
                        (slot == EquipmentSlot.RING && (item.getEquippedSlot() == EquipmentSlot.RING_1 || item.getEquippedSlot() == EquipmentSlot.RING_2)))
                .findFirst().orElse(null);
        Map<String, Integer> diff = new LinkedHashMap<>();
        diff.put("armorClass", candidate.getTemplate().getArmorClass() - (equipped == null ? 0 : equipped.getTemplate().getArmorClass()));
        diff.put("strength", candidate.getBonusStrength() - (equipped == null ? 0 : equipped.getBonusStrength()));
        diff.put("dexterity", candidate.getBonusDexterity() - (equipped == null ? 0 : equipped.getBonusDexterity()));
        diff.put("constitution", candidate.getBonusConstitution() - (equipped == null ? 0 : equipped.getBonusConstitution()));
        diff.put("intelligence", candidate.getBonusIntelligence() - (equipped == null ? 0 : equipped.getBonusIntelligence()));
        return new ItemComparisonResponse(toResponse(candidate), equipped == null ? null : toResponse(equipped), diff);
    }

}
