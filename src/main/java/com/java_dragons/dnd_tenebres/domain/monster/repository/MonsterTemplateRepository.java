package com.java_dragons.dnd_tenebres.domain.monster.repository;

import com.java_dragons.dnd_tenebres.domain.monster.entity.MonsterTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonsterTemplateRepository extends JpaRepository<MonsterTemplate, Long> {
    List<MonsterTemplate> findAllByBiomeAndLevel(String biome, int level);
}
