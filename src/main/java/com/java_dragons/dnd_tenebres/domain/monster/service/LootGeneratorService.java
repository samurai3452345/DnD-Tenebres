package com.java_dragons.dnd_tenebres.domain.monster.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
import com.java_dragons.dnd_tenebres.core.random.RandomSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;


import static java.util.Map.entry;

@Service
@RequiredArgsConstructor
public class LootGeneratorService {
    private final RandomSource randomSource;

    public Map<ItemTemplate, Integer> generateLootForMonster(MonsterTemplate template) {

        return template.getLootTable().stream()
                .filter(lootEntry -> DiceRoller.rollD100() <= lootEntry.getDropChance())
                .map(lootEntry -> {
                    if (lootEntry.getMinAmount() <= 0 || lootEntry.getMaxAmount() < lootEntry.getMinAmount()) {
                        throw new IllegalStateException("Некорректный диапазон добычи для монстра " + template.getName());
                    }
                    return entry(lootEntry.getItemTemplate(), randomSource.nextInt(
                            lootEntry.getMinAmount(), Math.addExact(lootEntry.getMaxAmount(), 1)));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Math::addExact));

    }

}
