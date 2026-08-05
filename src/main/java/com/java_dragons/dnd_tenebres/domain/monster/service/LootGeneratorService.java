package com.java_dragons.dnd_tenebres.domain.monster.service;

import com.java_dragons.dnd_tenebres.core.math.DiceRoller;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


import static java.util.Map.entry;

@Service
public class LootGeneratorService {
    public Map<ItemTemplate, Integer> generateLootForMonster(MonsterTemplate template) {

        return template.getLootTable().stream()
                .filter(lootEntry -> DiceRoller.rollD100() <= lootEntry.getDropChance())
                .map(lootEntry -> entry(lootEntry.getItemTemplate(), ThreadLocalRandom.current().nextInt(lootEntry.getMinAmount(), lootEntry.getMaxAmount() + 1)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }

}
