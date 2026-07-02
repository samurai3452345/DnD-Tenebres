package com.java_dragons.dnd_tenebres.domain.monster.service;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MonsterSpawnerService {
    private final MonsterRepository monsterRepository;



    private static final List<String> MONSTER_NAMES = List.of(
            "Дикий волк", "Скелет", "Гоблин", "Орк", "Зомби"
    );

    @Transactional
    public Monster spawnRandomMonster(int playerLevel){
        int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(MONSTER_NAMES.size());

        String name = MONSTER_NAMES.get(randomIndex);
        int maxHp = 20 + (playerLevel * 5);
        int armorClass = 10 + (playerLevel / 2);
        int xpReward = playerLevel + 15;
        int goldReward = playerLevel + 10;
        Set<DamageType> elements = new HashSet<>(Set.of(DamageType.PHYSICAL));
        Monster monster = new Monster(name, maxHp, armorClass, xpReward, goldReward, elements);


        return  monsterRepository.save(monster);
    }
}
