package com.java_dragons.dnd_tenebres.domain.location.repository;

import com.java_dragons.dnd_tenebres.domain.location.entity.LocationLootEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationLootEntryRepository extends JpaRepository<LocationLootEntry, Long> {
    List<LocationLootEntry> findByLocationId(String locationId);
}