package com.java_dragons.dnd_tenebres.domain.location.repository;

import com.java_dragons.dnd_tenebres.domain.location.entity.LocationRandomEncounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRandomEncounterRepository extends JpaRepository<LocationRandomEncounter, Long> {
    List<LocationRandomEncounter> findByLocationId(String locationId);
}