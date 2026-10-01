package com.java_dragons.dnd_tenebres.domain.player.repository;

import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStoryFlag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerStoryFlagRepository extends JpaRepository<PlayerStoryFlag, Long> {
    boolean existsByPlayerIdAndFlagCode(Long playerId, String flagCode);
}
