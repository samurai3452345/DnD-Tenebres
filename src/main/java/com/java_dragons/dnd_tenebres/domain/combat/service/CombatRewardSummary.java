package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatStateResponse.RewardState;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/** Reads already granted rewards; never grants or recalculates loot. */
public final class CombatRewardSummary {
    private static final Pattern ITEM = Pattern.compile("^Выбито: (.+) \\(x\\d+\\)$");
    private CombatRewardSummary() {}

    public static List<RewardState> fromEvents(List<CombatEvent> events) {
        var rewards = new LinkedHashMap<String, RewardState>();
        for (var event : events) {
            if (event.value() <= 0 || event.description() == null) continue;
            String name;
            String iconKey;
            if ("REWARD".equals(event.actionType()) && "Найдено золото".equals(event.description())) {
                name = "Золотые монеты";
                iconKey = "gold";
            } else if ("REWARD".equals(event.actionType()) && "Получен опыт".equals(event.description())) {
                name = "Опыт";
                iconKey = "experience";
            } else if ("LOOT".equals(event.actionType())) {
                var match = ITEM.matcher(event.description());
                if (!match.matches()) continue;
                name = match.group(1);
                iconKey = "item:" + name;
            } else continue;
            var previous = rewards.get(iconKey);
            rewards.put(iconKey, new RewardState(name, event.value() + (previous == null ? 0L : previous.amount()), iconKey));
        }
        return List.copyOf(rewards.values());
    }
}
