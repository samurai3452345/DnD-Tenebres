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
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.combat.strategy.ItemPassiveStrategy;
import com.java_dragons.dnd_tenebres.domain.combat.repository.SpellRepository;
import com.java_dragons.dnd_tenebres.domain.item.repository.MerchantOfferRepository;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GameContentValidator implements ApplicationRunner {
    private final LocationRepository locationRepository;
    private final ItemTemplateRepository itemTemplateRepository;
    private final List<MonsterSkillStrategy> monsterSkillStrategies;
    private final GamePlayerProperties gamePlayerProperties;
    private final List<ItemPassiveStrategy> passiveStrategies;
    private final SpellRepository spellRepository;
    private final MerchantOfferRepository merchantOfferRepository;
    private final com.java_dragons.dnd_tenebres.infrastructure.metrics.GameMetrics metrics;

    @Override @Transactional(readOnly = true) public void run(ApplicationArguments args) {
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
        Set<ItemPassive> passiveImplemented = new HashSet<>();
        passiveStrategies.forEach(strategy -> passiveImplemented.add(strategy.getTargetPassive()));
        Arrays.stream(ItemPassive.values()).filter(p -> p != ItemPassive.NONE && !passiveImplemented.contains(p))
                .forEach(p -> errors.add("Missing item passive strategy: " + p));
        spellRepository.findAll().forEach(spell -> {
            if (spell.getTier() < 1 || spell.getTier() > 5) errors.add("Invalid spell tier: " + spell.getName());
            if (spell.getManaCost() < 0 || spell.getDiceCount() < 0) errors.add("Invalid spell values: " + spell.getName());
        });
        if (merchantOfferRepository.findByLocationIdAndEnabledTrueOrderById("city_merch_guild").isEmpty())
            errors.add("Merchant has no offers");
        locationRepository.findAll().forEach(location -> location.getConnectedLocations().forEach(target -> {
            if (target == null || target.getId() == null) errors.add("Broken connection from " + location.getId());
        }));
        if (!errors.isEmpty()) { metrics.contentError(); throw new IllegalStateException("Invalid game content: " + String.join("; ", errors)); }
    }
}
