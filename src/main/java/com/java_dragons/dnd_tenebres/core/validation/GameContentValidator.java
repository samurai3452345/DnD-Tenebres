package com.java_dragons.dnd_tenebres.core.validation;

import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;
import com.java_dragons.dnd_tenebres.domain.combat.repository.SpellRepository;
import com.java_dragons.dnd_tenebres.domain.combat.strategy.ItemPassiveStrategy;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemPassive;
import com.java_dragons.dnd_tenebres.domain.item.repository.ItemTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.item.repository.MerchantOfferRepository;
import com.java_dragons.dnd_tenebres.domain.location.entity.LocationLootEntry;
import com.java_dragons.dnd_tenebres.domain.location.entity.LocationRandomEncounter;
import com.java_dragons.dnd_tenebres.domain.location.entity.LocationUnlockRequirement;
import com.java_dragons.dnd_tenebres.domain.location.model.UnlockRequirementType;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationLootEntryRepository;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRandomEncounterRepository;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRepository;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationUnlockRequirementRepository;
import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterLootEntry;
import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
import com.java_dragons.dnd_tenebres.domain.monster.model.MonsterSkill;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.monster.strategy.MonsterSkillStrategy;
import com.java_dragons.dnd_tenebres.domain.quest.entity.QuestTemplate;
import com.java_dragons.dnd_tenebres.domain.quest.repository.QuestTemplateRepository;
import com.java_dragons.dnd_tenebres.infrastructure.metrics.GameMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final LocationRandomEncounterRepository randomEncounterRepository;
    private final LocationLootEntryRepository locationLootEntryRepository;
    private final LocationUnlockRequirementRepository unlockRequirementRepository;
    private final MonsterTemplateRepository monsterTemplateRepository;
    private final QuestTemplateRepository questTemplateRepository;
    private final GameMetrics metrics;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        List<String> errors = new ArrayList<>();
        validateRequiredContent(errors);
        validateImplementedStrategies(errors);
        validateSpells(errors);
        validateLocationsAndEncounters(errors);
        validateLoot(errors);
        validateUnlockRequirements(errors);
        validateQuests(errors);

        if (!errors.isEmpty()) {
            metrics.contentError();
            throw new IllegalStateException("Invalid game content: " + String.join("; ", errors));
        }
    }

    private void validateRequiredContent(List<String> errors) {
        for (String id : List.of(gamePlayerProperties.getStartLocationId(), gamePlayerProperties.getRespawnLocationId())) {
            if (!locationRepository.existsById(id)) errors.add("Missing required location: " + id);
        }
        gamePlayerProperties.getStartItems().forEach(item -> {
            if (item.getAmount() <= 0) errors.add("Invalid starter amount: " + item.getTemplate());
            if (itemTemplateRepository.findByName(item.getTemplate()).isEmpty()) {
                errors.add("Missing starter item: " + item.getTemplate());
            }
        });
        if (merchantOfferRepository.findByLocationIdAndEnabledTrueOrderById("city_merch_guild").isEmpty()) {
            errors.add("Merchant has no offers");
        }
    }

    private void validateImplementedStrategies(List<String> errors) {
        Set<MonsterSkill> implemented = new HashSet<>();
        monsterSkillStrategies.forEach(strategy -> implemented.add(strategy.getTargetSkill()));
        Arrays.stream(MonsterSkill.values())
                .filter(skill -> skill != MonsterSkill.NONE && !implemented.contains(skill))
                .forEach(skill -> errors.add("Missing monster skill strategy: " + skill));

        Set<ItemPassive> passiveImplemented = new HashSet<>();
        passiveStrategies.forEach(strategy -> passiveImplemented.add(strategy.getTargetPassive()));
        Arrays.stream(ItemPassive.values())
                .filter(passive -> passive != ItemPassive.NONE && !passiveImplemented.contains(passive))
                .forEach(passive -> errors.add("Missing item passive strategy: " + passive));
    }

    private void validateSpells(List<String> errors) {
        spellRepository.findAll().forEach(spell -> {
            if (spell.getTier() < 1 || spell.getTier() > 5) errors.add("Invalid spell tier: " + spell.getName());
            if (spell.getManaCost() < 0 || spell.getDiceCount() < 0) {
                errors.add("Invalid spell values: " + spell.getName());
            }
        });
    }

    private void validateLocationsAndEncounters(List<String> errors) {
        locationRepository.findAll().forEach(location -> {
            if (location.getLevel() < 1) errors.add("Invalid location level: " + location.getId());
            if (location.getHuntDifficulty() < 0 || location.getSearchDifficulty() < 0) {
                errors.add("Invalid location difficulty: " + location.getId());
            }
            location.getConnectedLocations().forEach(target -> {
                if (target == null || target.getId() == null) errors.add("Broken connection from " + location.getId());
            });
        });

        Map<String, Integer> totalWeights = new HashMap<>();
        for (LocationRandomEncounter encounter : randomEncounterRepository.findAll()) {
            if (!locationRepository.existsById(encounter.getLocationId())) {
                errors.add("Random encounter references missing location: " + encounter.getLocationId());
            }
            if (monsterTemplateRepository.findByName(encounter.getMonsterTemplateName()).isEmpty()) {
                errors.add("Random encounter references missing monster: " + encounter.getMonsterTemplateName());
            }
            if (encounter.getSpawnChance() <= 0) {
                errors.add("Random encounter weight must be positive: " + encounter.getId());
            }
            totalWeights.merge(encounter.getLocationId(), encounter.getSpawnChance(), Integer::sum);
        }
        totalWeights.forEach((location, total) -> {
            if (total <= 0) errors.add("Random encounter total weight must be positive: " + location);
        });
    }

    private void validateLoot(List<String> errors) {
        Set<String> locationItems = new HashSet<>();
        for (LocationLootEntry entry : locationLootEntryRepository.findAll()) {
            String itemId = entry.getItemTemplate() == null ? "null" : String.valueOf(entry.getItemTemplate().getId());
            validateLootRange("location " + entry.getLocationId(), entry.getMinAmount(), entry.getMaxAmount(),
                    entry.getFindChance(), errors);
            if (!locationItems.add(entry.getLocationId() + ':' + itemId)) {
                errors.add("Duplicate location loot entry: " + entry.getLocationId() + '/' + itemId);
            }
        }

        for (MonsterTemplate template : monsterTemplateRepository.findAll()) {
            Set<Long> monsterItems = new HashSet<>();
            for (MonsterLootEntry entry : template.getLootTable()) {
                validateLootRange("monster " + template.getName(), entry.getMinAmount(), entry.getMaxAmount(),
                        entry.getDropChance(), errors);
                Long itemId = entry.getItemTemplate() == null ? null : entry.getItemTemplate().getId();
                if (itemId == null) errors.add("Monster loot has no item: " + template.getName());
                else if (!monsterItems.add(itemId)) errors.add("Duplicate monster loot entry: " + template.getName() + '/' + itemId);
            }
        }
    }

    private void validateLootRange(String owner, int min, int max, int chance, List<String> errors) {
        if (min <= 0 || max < min) errors.add("Invalid loot amount range for " + owner);
        if (chance < 0 || chance > 100) errors.add("Invalid loot chance for " + owner);
    }

    private void validateUnlockRequirements(List<String> errors) {
        for (LocationUnlockRequirement requirement : unlockRequirementRepository.findAll()) {
            if (!locationRepository.existsById(requirement.getFromLocationId())
                    || !locationRepository.existsById(requirement.getToLocationId())) {
                errors.add("Unlock requirement references missing location: " + requirement.getId());
            }
            if (requirement.getBlockedReason() == null || requirement.getBlockedReason().isBlank()) {
                errors.add("Unlock requirement has no blocked reason: " + requirement.getId());
            }
            validateRequirementValue(requirement, errors);
        }
    }

    private void validateRequirementValue(LocationUnlockRequirement requirement, List<String> errors) {
        String value = requirement.getValue();
        try {
            if (requirement.getType() == UnlockRequirementType.MIN_LEVEL && Integer.parseInt(value) < 1) {
                errors.add("Invalid minimum level requirement: " + requirement.getId());
            } else if (requirement.getType() == UnlockRequirementType.QUEST_COMPLETED
                    && !questTemplateRepository.existsById(Long.parseLong(value))) {
                errors.add("Unlock requirement references missing quest: " + requirement.getId());
            } else if (requirement.getType() == UnlockRequirementType.ITEM_OWNED
                    && itemTemplateRepository.findByName(value).isEmpty()) {
                errors.add("Unlock requirement references missing item: " + requirement.getId());
            } else if (requirement.getType() == UnlockRequirementType.LOCATION_CLEARED
                    && !locationRepository.existsById(value)) {
                errors.add("Unlock requirement references missing cleared location: " + requirement.getId());
            } else if (requirement.getType() == UnlockRequirementType.STORY_FLAG
                    && (value == null || value.isBlank())) {
                errors.add("Unlock requirement has empty story flag: " + requirement.getId());
            }
        } catch (RuntimeException exception) {
            errors.add("Invalid unlock requirement value: " + requirement.getId());
        }
    }

    private void validateQuests(List<String> errors) {
        List<QuestTemplate> quests = questTemplateRepository.findAll();
        Map<Long, Long> prerequisites = new HashMap<>();
        for (QuestTemplate quest : quests) {
            if (quest.getTargetCount() <= 0 || quest.getMinLevel() < 1
                    || quest.getRewardXp() < 0 || quest.getRewardGold() < 0 || quest.getRewardItemAmount() < 0) {
                errors.add("Invalid quest values: " + quest.getId());
            }
            validateQuestLocation(quest.getAcceptLocationId(), "accept", quest.getId(), errors);
            validateQuestLocation(quest.getTurnInLocationId(), "turn-in", quest.getId(), errors);
            if (quest.getPrerequisiteQuestId() != null) {
                if (!questTemplateRepository.existsById(quest.getPrerequisiteQuestId())) {
                    errors.add("Quest references missing prerequisite: " + quest.getId());
                }
                prerequisites.put(quest.getId(), quest.getPrerequisiteQuestId());
            }
            if (quest.getRewardItemTemplateId() != null
                    && !itemTemplateRepository.existsById(quest.getRewardItemTemplateId())) {
                errors.add("Quest references missing reward item: " + quest.getId());
            }
        }
        validateQuestCycles(prerequisites, errors);
    }

    private void validateQuestLocation(String locationId, String kind, Long questId, List<String> errors) {
        if (locationId != null && !locationRepository.existsById(locationId)) {
            errors.add("Quest " + kind + " location is missing: " + questId + '/' + locationId);
        }
    }

    private void validateQuestCycles(Map<Long, Long> prerequisites, List<String> errors) {
        Set<Long> reported = new HashSet<>();
        for (Long start : prerequisites.keySet()) {
            Set<Long> path = new HashSet<>();
            Long current = start;
            while (current != null && path.add(current)) current = prerequisites.get(current);
            if (current != null && reported.add(current)) errors.add("Quest prerequisite cycle contains: " + current);
        }
    }
}
