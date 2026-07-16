package com.java_dragons.dnd_tenebres.domain.monster.repository;

import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonsterRepository extends JpaRepository<Monster, Long> {
}
