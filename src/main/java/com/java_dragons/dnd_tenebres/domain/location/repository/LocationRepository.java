package com.java_dragons.dnd_tenebres.domain.location.repository;

import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, String> {
}
