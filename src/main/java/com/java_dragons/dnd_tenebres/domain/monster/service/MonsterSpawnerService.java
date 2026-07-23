package com.java_dragons.dnd_tenebres.domain.monster.service;

import com.java_dragons.dnd_tenebres.domain.location.entity.LocationFixedMonster;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationFixedMonsterRepository;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterRepository;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MonsterSpawnerService {
    private final MonsterRepository monsterRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final LocationFixedMonsterRepository locationFixedMonsterRepository;

    @Transactional
    public Monster spawnRandomMonster(String biome, int locationLevel) {
        List<MonsterTemplate> templates =
                monsterTemplateRepository.findAllByBiomeAndLevel(biome, locationLevel);

        if (templates.isEmpty()) {
            throw new IllegalArgumentException("Нет шаблонов монстров для биома: " + biome + " и уровня: " + locationLevel);
        }

        MonsterTemplate template = templates.get(ThreadLocalRandom.current().nextInt(templates.size()));

        Monster monster = Monster.builder()
                .name(template.getName())
                .level(template.getLevel())
                .currentHp(template.getBaseHp())
                .maxHp(template.getBaseHp())
                .armorClass(template.getArmorClass())
                .xpReward(template.getXpReward())
                .goldReward(template.getGoldReward())
                .damageDice(template.getDamageDice())
                .damageBonus(template.getDamageBonus())
                .attackName(template.getAttackName())
                .elements(new HashSet<>(template.getElements()))
                .specialSkill(template.getSpecialSkill())
                .skillFrequency(template.getSkillFrequency())
                .resistances(new HashSet<>(template.getResistances()))
                .build();

        return monsterRepository.save(monster);
    }

    @Transactional
    public List<Monster> spawnFixedMonstersForLocation(String locationId) {
        List<Monster> squad = new ArrayList<>();
        List<LocationFixedMonster> fixedMonsters = locationFixedMonsterRepository.findByLocationId(locationId);

        for (LocationFixedMonster fm : fixedMonsters) {
            MonsterTemplate template = monsterTemplateRepository.findByName(fm.getMonsterTemplateName())
                    .orElseThrow(() -> new RuntimeException("Шаблон не найден: " + fm.getMonsterTemplateName()));

            for (int i = 0; i < fm.getCount(); i++) {
                Monster monster = Monster.builder()
                        .name(template.getName() + (fm.getCount() > 1 ? " #" + (i+1) : ""))
                        .level(template.getLevel())
                        .maxHp(template.getBaseHp())
                        .currentHp(template.getBaseHp())
                        .armorClass(template.getArmorClass())
                        .xpReward(template.getXpReward())
                        .goldReward(template.getGoldReward())
                        .damageDice(template.getDamageDice())
                        .damageBonus(template.getDamageBonus())
                        .attackName(template.getAttackName())
                        .elements(new HashSet<>(template.getElements()))
                        .specialSkill(template.getSpecialSkill())
                        .skillFrequency(template.getSkillFrequency())
                        .resistances(new HashSet<>(template.getResistances()))
                        .build();
                squad.add(monsterRepository.save(monster));
            }
        }
        return squad;
    }
}