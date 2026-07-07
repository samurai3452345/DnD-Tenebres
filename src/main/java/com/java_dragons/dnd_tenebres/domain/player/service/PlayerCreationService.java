package com.java_dragons.dnd_tenebres.domain.player.service;


import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PlayerCreationService {

    private final ProgressionCalculator progressionCalculator;

    private static final Map<Integer, Integer> POINT_BUY_COSTS = Map.of(
            8,0,
            9,1,
            10,2,
            11,3,
            12,4,
            13,5,
            14,7,
            15,9
    );

    public Player createCharacter(PlayerCreationRequest request){
        List<Integer> abilities = List.of(request.strength(),
                request.dexterity(),
                request.constitution(),
                request.intelligence(),
                request.wisdom(),
                request.charisma());

        boolean hasInvalidUpgrade = abilities.stream().
                anyMatch(upgrade -> upgrade < 8 || upgrade > 15);

        if (hasInvalidUpgrade) {
            throw new IllegalArgumentException("Характеристики должны быть от 8 до 15!");
        }

        int totalPointSpend = abilities.stream()
                .mapToInt(POINT_BUY_COSTS::get)
                .sum();

        if(totalPointSpend != 27){
            throw new IllegalArgumentException("Неверно распределены очки!: " + totalPointSpend + " а нужно 27!");
        }


        PlayerStats stats = new PlayerStats(
                request.strength(),
                request.dexterity(),
                request.constitution(),
                request.intelligence(),
                request.wisdom(),
                request.charisma());

        return Player.builder()
                .name(request.name())
                .stats(stats)
                .currentHp(progressionCalculator.getHeroBaseHp(1))
                .build();
    }
}
