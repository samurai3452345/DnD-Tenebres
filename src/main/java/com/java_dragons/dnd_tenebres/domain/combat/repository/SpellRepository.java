package com.java_dragons.dnd_tenebres.domain.combat.repository;

import com.java_dragons.dnd_tenebres.domain.combat.entity.Spell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpellRepository extends JpaRepository<Spell, Long> {
    Optional<Spell> findByName(String name);
    List<Spell> findByTier(int tier);
}