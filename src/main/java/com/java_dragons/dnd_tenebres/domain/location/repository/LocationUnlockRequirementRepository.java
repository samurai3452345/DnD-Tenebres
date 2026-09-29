package com.java_dragons.dnd_tenebres.domain.location.repository;

import com.java_dragons.dnd_tenebres.domain.location.entity.LocationUnlockRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationUnlockRequirementRepository extends JpaRepository<LocationUnlockRequirement, Long> {
    List<LocationUnlockRequirement> findByFromLocationIdAndToLocationId(String fromLocationId, String toLocationId);
}
