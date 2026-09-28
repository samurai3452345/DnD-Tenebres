package com.java_dragons.dnd_tenebres.domain.item.repository;

import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerItemRepository extends JpaRepository<PlayerItem, Long> {
    @EntityGraph(attributePaths = "template")
    List<PlayerItem> findByPlayerId(Long playerId);
    Optional<PlayerItem> findByIdAndPlayerId(Long id, Long playerId);
}
