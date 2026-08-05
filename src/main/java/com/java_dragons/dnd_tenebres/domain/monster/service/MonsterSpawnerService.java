package com.java_dragons.dnd_tenebres.domain.monster.service;

import com.java_dragons.dnd_tenebres.domain.location.entity.LocationFixedMonster;
import com.java_dragons.dnd_tenebres.domain.location.entity.LocationRandomEncounter;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationFixedMonsterRepository;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRandomEncounterRepository;
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
    private final LocationRandomEncounterRepository randomEncounterRepository;

    @Transactional
    public Monster spawnRandomMonster(String locationId) {
        // 1. Получаем список встреч для конкретной локации
        List<LocationRandomEncounter> encounters = randomEncounterRepository.findByLocationId(locationId);

        if (encounters.isEmpty()) {
            throw new IllegalArgumentException("В локации " + locationId + " нет случайных встреч!");
        }

        // 2. Считаем общий вес шансов
        int totalWeight = encounters.stream().mapToInt(LocationRandomEncounter::getSpawnChance).sum();

        // 3. Бросаем кубик от 0 до totalWeight - 1
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentSum = 0;
        String chosenMonsterName = null;

        // 4. Определяем, кто выпал (алгоритм рулетки)
        for (LocationRandomEncounter encounter : encounters) {
            currentSum += encounter.getSpawnChance();
            if (roll < currentSum) {
                chosenMonsterName = encounter.getMonsterTemplateName();
                break;
            }
        }

        // 5. Загружаем шаблон выпавшего монстра из БД
        // Создаем финальную копию переменной специально для лямбды
        final String finalMonsterName = chosenMonsterName;

        MonsterTemplate template = monsterTemplateRepository.findByName(finalMonsterName)
                .orElseThrow(() -> new IllegalStateException("Шаблон монстра не найден: " + finalMonsterName));

        // 6. Создаем физического монстра
        Monster monster = Monster.builder()
                .name(template.getName())
                .templateName(template.getName())
                .level(template.getLevel())
                .currentHp(template.getBaseHp())
                .maxHp(template.getBaseHp())
                .armorClass(template.getArmorClass())
                .xpReward(template.getXpReward())
                .goldReward(template.getGoldReward())
                .damageDice(template.getDamageDice())
                .diceCount(template.getDiceCount())
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
                        .templateName(template.getName())
                        .level(template.getLevel())
                        .maxHp(template.getBaseHp())
                        .currentHp(template.getBaseHp())
                        .armorClass(template.getArmorClass())
                        .xpReward(template.getXpReward())
                        .goldReward(template.getGoldReward())
                        .damageDice(template.getDamageDice())
                        .diceCount(template.getDiceCount())
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