package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.player.dto.LevelUpResult;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerProgressionService {
    private final ProgressionCalculator progressionCalculator;

    public LevelUpResult grantExperience(Player player, long amount) {
        if (amount < 0) throw new IllegalArgumentException("Experience cannot be negative");
        int oldLevel = player.getLevel();
        int oldPoints = player.getStatPoints();
        player.addExperience(amount);
        while (player.getLevel() < 16
                && player.getExperience() >= progressionCalculator.getRequiredXpForLevel(player.getLevel() + 1)) {
            int nextLevel = player.getLevel() + 1;
            player.levelUp(
                    progressionCalculator.getHeroBaseHp(nextLevel),
                    progressionCalculator.getHeroBaseMp(nextLevel));
        }
        return new LevelUpResult(amount, oldLevel, player.getLevel(), player.getStatPoints() - oldPoints);
    }
}
