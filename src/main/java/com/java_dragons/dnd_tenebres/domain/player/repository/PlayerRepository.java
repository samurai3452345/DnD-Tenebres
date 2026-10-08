package com.java_dragons.dnd_tenebres.domain.player.repository;

import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select player from Player player where player.id = :id")
    Optional<Player> findByIdForUpdate(@Param("id") Long id);
    Optional<Player> findByName(String name);

    long countByAccountId(Long accountId);

    boolean existsByIdAndAccountId(Long id, Long accountId);

    Optional<Player> findByIdAndAccountId(Long id, Long accountId);

    @EntityGraph(attributePaths = "currentLocation")
    List<Player> findAllByAccountIdOrderByIdAsc(Long accountId);
}
