package com.java_dragons.dnd_tenebres.domain.location.repository;

import com.java_dragons.dnd_tenebres.domain.location.entity.LocationFixedMonster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationFixedMonsterRepository extends JpaRepository<LocationFixedMonster, Long> {
    List<LocationFixedMonster> findByLocationId(String locationId);
}
