package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRepository;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerDeathReport;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import com.java_dragons.dnd_tenebres.core.config.GamePlayerProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.java_dragons.dnd_tenebres.domain.economy.service.WalletService;
import com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason;

@Service
@RequiredArgsConstructor
public class PlayerDeathService {
    private final LocationRepository locationRepository;
    private final GamePlayerProperties gamePlayerProperties;
    private final WalletService walletService;

    @Transactional
    public PlayerDeathReport handleDeath(Player player) {
        if (player.getCurrentHp() > 0) throw new IllegalStateException("Cannot respawn a living player");
        long lost = (player.getGold() * gamePlayerProperties.getDeathGoldPenaltyPercent()) / 100;
        if (lost > 0) walletService.debit(player, lost, WalletReason.DEATH_PENALTY, "PLAYER", player.getId().toString());
        player.clearEffects();
        String respawnLocationId = gamePlayerProperties.getRespawnLocationId();
        player.moveTo(locationRepository.findById(respawnLocationId)
                .orElseThrow(() -> new IllegalStateException("Respawn location is missing: " + respawnLocationId)));
        player.restoreAfterDeath();
        return new PlayerDeathReport(lost, respawnLocationId, player.getCurrentHp(), player.getCurrentMp());
    }
}
