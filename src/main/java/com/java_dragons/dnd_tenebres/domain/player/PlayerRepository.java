package com.java_dragons.dnd_tenebres.domain.player;

import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

 interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);
}
