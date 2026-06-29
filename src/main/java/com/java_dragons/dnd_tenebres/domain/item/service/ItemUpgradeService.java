package com.java_dragons.dnd_tenebres.domain.item.service;



import com.java_dragons.dnd_tenebres.core.math.ItemProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.item.entity.Artifact;
import com.java_dragons.dnd_tenebres.domain.item.entity.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemUpgradeService {

    private final ItemRepository itemRepository;
    private final ItemProgressionCalculator itemProgressionCalculator;

    @Transactional
    public void feedItems(Long playerId, Long targetItemId, List<Long> foodItemIds ) {

        Item itemTarget = itemRepository.findById(targetItemId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден!"));

        if(!itemTarget.getPlayer().getId().equals(playerId)) {
            throw new  IllegalArgumentException("Это не твой предмет!");
        }
        if(itemTarget instanceof Artifact){
            throw new IllegalArgumentException("Артефакты нельзя прокачивать!");
        }
        if (foodItemIds.contains(targetItemId)){
            throw new IllegalArgumentException("Нельзя скармливать предмет самому себе!");
        }

        List<Item> foodItems = itemRepository.findAllById(foodItemIds);

        if(!foodItems.stream().allMatch(foodItem -> foodItem.getPlayer().getId().equals(playerId))){
            throw new  IllegalArgumentException("Это не твой предмет!");
        }

        for(Item foodItem : foodItems){
            int xpYield = itemProgressionCalculator.calculateXpYield(
                    itemTarget.getItemType(), itemTarget.getRarity(),
                    foodItem.getItemType(), foodItem.getRarity()
            );
            itemTarget.addXp(xpYield);

        }
        itemRepository.deleteAll(foodItems);
        itemRepository.save(itemTarget);


    }

}
