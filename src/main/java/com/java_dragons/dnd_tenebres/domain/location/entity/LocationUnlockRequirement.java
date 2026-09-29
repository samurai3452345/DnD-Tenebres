package com.java_dragons.dnd_tenebres.domain.location.entity;

import com.java_dragons.dnd_tenebres.domain.location.model.UnlockRequirementType;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "location_unlock_requirements")
@Getter
public class LocationUnlockRequirement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "from_location_id", nullable = false)
    private String fromLocationId;
    @Column(name = "to_location_id", nullable = false)
    private String toLocationId;
    @Enumerated(EnumType.STRING) @Column(name = "requirement_type", nullable = false)
    private UnlockRequirementType type;
    @Column(name = "requirement_value", nullable = false)
    private String value;
    @Column(name = "blocked_reason", nullable = false)
    private String blockedReason;
}
