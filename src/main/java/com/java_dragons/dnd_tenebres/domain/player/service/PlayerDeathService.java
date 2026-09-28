package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRepository;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerDeathReport;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerDeathService {
    private final LocationRepository locationRepository;
    private final GamePlayerProperties gamePlayerProperties;

    @Transactional
    public PlayerDeathReport handleDeath(Player player) {
        if (player.getCurrentHp() > 0) throw new IllegalStateException("Cannot respawn a living player");
        long lost = player.removeGoldPercent(gamePlayerProperties.getDeathGoldPenaltyPercent());
        player.leaveCombat();
        player.clearEffects();
        String respawnLocationId = gamePlayerProperties.getRespawnLocationId();
        player.moveTo(locationRepository.findById(respawnLocationId)
                .orElseThrow(() -> new IllegalStateException("Respawn location is missing: " + respawnLocationId)));
        player.restoreAfterDeath();
        return new PlayerDeathReport(lost, respawnLocationId, player.getCurrentHp(), player.getCurrentMp());
    }
}
