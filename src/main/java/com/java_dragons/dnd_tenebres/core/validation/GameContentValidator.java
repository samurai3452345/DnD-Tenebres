package com.java_dragons.dnd_tenebres.core.validation;

import com.java_dragons.dnd_tenebres.domain.item.repository.ItemTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRepository;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import com.java_dragons.dnd_tenebres.domain.monster.strategy.MonsterSkillStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
@RequiredArgsConstructor
public class GameContentValidator implements ApplicationRunner {
    private final LocationRepository locationRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final List<MonsterSkillStrategy> monsterSkillStrategies;
    private final GamePlayerProperties gamePlayerProperties;

    @Override public void run(ApplicationArguments args) {
        List<String> errors = new ArrayList<>();
        for (String id : List.of(gamePlayerProperties.getStartLocationId(), gamePlayerProperties.getRespawnLocationId()))
            if (!locationRepository.existsById(id)) errors.add("Missing required location: " + id);
        gamePlayerProperties.getStartItems().forEach(item -> {
            if (item.getAmount() <= 0) errors.add("Invalid starter amount: " + item.getTemplate());
            if (itemTemplateRepository.findByName(item.getTemplate()).isEmpty()) errors.add("Missing starter item: " + item.getTemplate());
        });
        Set<MonsterSkill> implemented = new HashSet<>();
        monsterSkillStrategies.forEach(strategy -> implemented.add(strategy.getTargetSkill()));
        Arrays.stream(MonsterSkill.values()).filter(skill -> skill != MonsterSkill.NONE && !implemented.contains(skill))
                .forEach(skill -> errors.add("Missing monster skill strategy: " + skill));
        if (!errors.isEmpty()) throw new IllegalStateException("Invalid game content: " + String.join("; ", errors));
    }
}
