package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.repository.ItemTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.item.repository.PlayerItemRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final PlayerRepository playerRepository;
    private final PlayerItemRepository playerItemRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final InventoryService inventoryService;

    private static final String MERCHANT_LOCATION_ID = "city_merch_guild";

    @Transactional
    public String buyItem(Long playerId, String templateName, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Количество должно быть больше нуля");

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        validateLocation(player);

        ItemTemplate template = itemTemplateRepository.findByName(templateName)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        if (template.getType() != ItemType.CONSUMABLE) {
            throw new IllegalArgumentException("Торговец продает только расходники и зелья!");
        }

        int price = calculateBuyPrice(template) * amount;

        if (!player.spendGold(price)) {
            throw new IllegalStateException("Недостаточно золота! Требуется: " + price);
        }

        inventoryService.addItemToPlayer(player, templateName, amount);
        return String.format("Вы успешно купили %s (x%d) за %d золотых.", templateName, amount, price);
    }

    @Transactional
    public String sellItem(Long playerId, Long playerItemId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Количество должно быть больше нуля");

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Игрок не найден"));

        validateLocation(player);

        PlayerItem item = playerItemRepository.findById(playerItemId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));

        if (!item.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException("Это не ваш предмет!");
        }
        if (item.isEquipped()) {
            throw new IllegalStateException("Сначала снимите предмет, прежде чем продавать его!");
        }
        if (item.getAmount() < amount) {
            throw new IllegalArgumentException("У вас нет такого количества предметов!");
        }

        int price = calculateSellPrice(item.getTemplate()) * amount;
        player.addGold(price);

        item.setAmount(item.getAmount() - amount);
        if (item.getAmount() <= 0) {
            player.getInventory().remove(item);
            playerItemRepository.delete(item);
        }

        return String.format("Вы продали %s (x%d) за %d золотых.", item.getTemplate().getName(), amount, price);
    }

    private void validateLocation(Player player) {
        if (!player.getCurrentLocation().getId().equals(MERCHANT_LOCATION_ID)) {
            throw new IllegalStateException("Для торговли нужно находиться в Гильдии Торговцев (city_merch_guild)!");
        }
        if (player.isInCombat()) {
            throw new IllegalStateException("Нельзя торговать во время боя!");
        }
    }

    private int calculateBuyPrice(ItemTemplate template) {
        return template.getStatBudget() * 2 + 5;
    }

    private int calculateSellPrice(ItemTemplate template) {
        int basePrice = template.getStatBudget() * 2 + 5;

        if (template.getType() == ItemType.RESOURCE) {
            return template.getRarity().getTierIndex() * 10;
        }

        return Math.max(1, (basePrice / 4) + (template.getRarity().getTierIndex() * 15));
    }
}