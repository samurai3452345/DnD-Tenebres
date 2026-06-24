package com.java_dragons.dnd_tenebres.player;

import com.java_dragons.dnd_tenebres.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

 interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByName(String name);
}
