package com.java_dragons.dnd_tenebres.domain.location.service;

import com.java_dragons.dnd_tenebres.domain.item.repository.PlayerItemRepository;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.repository.*;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerStoryFlagRepository;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestStatus;
import com.java_dragons.dnd_tenebres.domain.quest.repository.PlayerQuestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LocationUnlockService {
    private final LocationUnlockRequirementRepository requirementRepository;
    private final PlayerQuestRepository questRepository;
    private final PlayerItemRepository itemRepository;
    private final PlayerClearedLocationRepository clearedRepository;
    private final PlayerStoryFlagRepository flagRepository;

    public AccessResult check(Player player, Location from, Location target) {
        List<String> reasons = new ArrayList<>();
        if (player.getLevel() < target.getLevel()) reasons.add("Требуется уровень " + target.getLevel());
        for (var requirement : requirementRepository.findByFromLocationIdAndToLocationId(from.getId(), target.getId())) {
            boolean met = switch (requirement.getType()) {
                case MIN_LEVEL -> player.getLevel() >= Integer.parseInt(requirement.getValue());
                case QUEST_COMPLETED -> questRepository.existsByPlayerIdAndQuestTemplateIdAndQuestStatus(
                        player.getId(), Long.parseLong(requirement.getValue()), QuestStatus.REWARDED);
                case ITEM_OWNED -> itemRepository.existsByPlayerIdAndTemplateNameAndAmountGreaterThan(
                        player.getId(), requirement.getValue(), 0);
                case LOCATION_CLEARED -> clearedRepository.existsByPlayerIdAndLocationId(player.getId(), requirement.getValue());
                case STORY_FLAG -> flagRepository.existsByPlayerIdAndFlagCode(player.getId(), requirement.getValue());
            };
            if (!met) reasons.add(requirement.getBlockedReason());
        }
        return new AccessResult(reasons.isEmpty(), List.copyOf(reasons));
    }

    public void requireAccess(Player player, Location from, Location target) {
        AccessResult result = check(player, from, target);
        if (!result.open()) throw new IllegalStateException(String.join("; ", result.blockedReasons()));
    }

    public record AccessResult(boolean open, List<String> blockedReasons) {}
}
