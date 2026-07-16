package com.java_dragons.dnd_tenebres.domain.monster.repository;

import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonsterTemplateRepository extends JpaRepository<MonsterTemplate, Long> {
    List<MonsterTemplate> findAllByBiomeAndLevel(String biome, int level);

    Optional<MonsterTemplate> findByName(String name);
}
