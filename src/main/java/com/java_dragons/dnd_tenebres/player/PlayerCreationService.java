package com.java_dragons.dnd_tenebres.player;


import com.java_dragons.dnd_tenebres.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.player.entity.Player;
import com.java_dragons.dnd_tenebres.player.entity.PlayerStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PlayerCreationService {
    private final PlayerRepository playerRepository;
    private final Map<Integer, Integer> priceUpgrade = Map.of(
            8,0,
            9,1,
            10,2,
            11,3,
            12,4,
            13,5,
            14,7,
            15,9
    );
    @Transactional
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

        int totalPointSpend = abilities.stream().mapToInt(stat -> priceUpgrade.get(stat)).sum();

        if(totalPointSpend != 27){
            throw new IllegalArgumentException("Неверно распределены очки!: " + totalPointSpend + " а нужно 27!");
        }


        PlayerStats stats = new PlayerStats(request.strength(),
                request.dexterity(),
                request.constitution(),
                request.intelligence(),
                request.wisdom(),
                request.charisma());

        Player player = Player.builder()
                .name(request.name())
                .stats(stats)
                .build();




        return playerRepository.save(player);
    }

}
