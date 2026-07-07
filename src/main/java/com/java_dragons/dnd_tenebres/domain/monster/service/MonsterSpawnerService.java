package com.java_dragons.dnd_tenebres.domain.monster.service;


import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterRepository;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MonsterSpawnerService {
    private final MonsterRepository monsterRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;

    @Transactional
    public Monster spawnRandomMonster(String biome, int locationLevel) {
        List<MonsterTemplate> templates =
                monsterTemplateRepository.findAllByBiomeAndLevel(biome, locationLevel);
        if(templates.isEmpty()) {
            throw new IllegalArgumentException("Нет шаблонов монстров для данного биома и уровня!");
        }

       MonsterTemplate monsterTemplate = templates.get(ThreadLocalRandom.current().nextInt(templates.size()));

        Monster monster = new  Monster(
                monsterTemplate.getName(),
                monsterTemplate.getBaseHp(),
                monsterTemplate.getArmorClass(),
                monsterTemplate.getXpReward(),
                monsterTemplate.getGoldReward(),
                new HashSet<>(monsterTemplate.getElements())
        );

        return monsterRepository.save(monster);
    }
}
