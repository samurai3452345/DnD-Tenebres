package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.core.math.ItemProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem; // Обрати внимание, теперь тут PlayerItem
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.repository.PlayerItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ItemUpgradeService {

    private final PlayerItemRepository playerItemRepository;
    private final ItemProgressionCalculator itemProgressionCalculator;

    @Transactional
    public void feedItems(Long playerId, Long targetItemId, List<Long> foodItemIds) {

        if (foodItemIds == null || foodItemIds.isEmpty()) {
            throw new IllegalArgumentException("Список предметов для поглощения не может быть пустым");
        }
        if (new HashSet<>(foodItemIds).size() != foodItemIds.size()) {
            throw new IllegalArgumentException("Список предметов для поглощения содержит повторяющиеся ID");
        }

        PlayerItem itemTarget = playerItemRepository.findById(targetItemId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден!"));

        if(!itemTarget.getPlayer().getId().equals(playerId)) {
            throw new IllegalArgumentException("Это не твой предмет!");
        }

        ItemType targetType = itemTarget.getTemplate().getType();
        if(targetType == ItemType.ARTIFACT || targetType == ItemType.CONSUMABLE || targetType == ItemType.RESOURCE){
            throw new IllegalArgumentException("Этот тип предмета нельзя прокачивать!");
        }

        if (foodItemIds.contains(targetItemId)){
            throw new IllegalArgumentException("Нельзя скармливать предмет самому себе!");
        }

        List<PlayerItem> foodItems = playerItemRepository.findAllById(foodItemIds);

        if(foodItems.size() != foodItemIds.size()){
            throw new IllegalArgumentException("Один или несколько предметов из списка корма не найдены в БД!");
        }

        if(!foodItems.stream().allMatch(foodItem -> foodItem.getPlayer().getId().equals(playerId))){
            throw new IllegalArgumentException("Один из предметов для скармливания тебе не принадлежит!");
        }

        if (foodItems.stream().anyMatch(PlayerItem::isEquipped)) {
            throw new IllegalStateException("Сначала снимите все предметы, выбранные для поглощения");
        }

        for(PlayerItem foodItem : foodItems){
            int xpYield = itemProgressionCalculator.calculateXpYield(
                    itemTarget.getTemplate().getType(), itemTarget.getTemplate().getRarity(),
                    foodItem.getTemplate().getType(), foodItem.getTemplate().getRarity()
            );

            long projectedXp = itemTarget.getItemXp() + xpYield;
            int newTier = itemProgressionCalculator.getTierByXp(projectedXp);

            itemTarget.addXp(xpYield, newTier);
        }

        playerItemRepository.deleteAll(foodItems);
    }
}
