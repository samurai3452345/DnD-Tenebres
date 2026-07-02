package com.java_dragons.dnd_tenebres.domain.monster.service;

import com.java_dragons.dnd_tenebres.domain.combat.model.DamageType;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class MonsterSpawnerService {
    private final MonsterRepository monsterRepository;



    private static String[] MONSTER_NAMES = {"Дикий волк","Скелет","Гоблин","Орк","Зомби"};

    @Transactional
    public Monster spawnRandomMonster(int playerLevel){
        int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(MONSTER_NAMES.length);

        Monster monster = new Monster();
        monster.setName(MONSTER_NAMES[randomIndex]);
        monster.setMaxHp(20+(playerLevel*5));
        monster.setCurrentHp(monster.getMaxHp());
        monster.setArmorClass(10+(playerLevel/2));
        monster.setXpReward(playerLevel + 15);
        monster.setGoldReward(playerLevel + 10);
        monster.getElements().add(DamageType.PHYSICAL); //пока пусть будет физический тип


        return  monsterRepository.save(monster);
    }
}
