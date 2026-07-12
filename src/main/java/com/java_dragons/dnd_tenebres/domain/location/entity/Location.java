package com.java_dragons.dnd_tenebres.domain.location.entity;

import com.java_dragons.dnd_tenebres.domain.location.model.BiomeType;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
public class Location {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "zone_id", nullable = false)
    private String zoneId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LocationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect")
    private LocationEffect effect = LocationEffect.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "biome", nullable = false)
    private BiomeType biome;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "location_connections",
            joinColumns = @JoinColumn(name = "from_location_id"),
            inverseJoinColumns = @JoinColumn(name = "to_location_id")
    )
    private Set<Location> connectedLocations = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false; Location location = (Location) o;
        return id != null && id.equals(location.getId());
    }

    @Override
    public int hashCode(){
        return getClass().hashCode();
    }



}
