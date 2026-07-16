package com.java_dragons.dnd_tenebres.domain.item.repository;

import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerItemRepository extends JpaRepository<PlayerItem, Long> {
    List<PlayerItem> findByPlayerId(Long playerId);
}
