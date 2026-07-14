package com.java_dragons.dnd_tenebres.domain.location.repository;

import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, String> {

    @Query("SELECT l FROM Location l LEFT JOIN FETCH l.connectedLocations WHERE l.id = :id")
    Optional<Location> findByIdWithConnections(@Param("id") String id);
}
