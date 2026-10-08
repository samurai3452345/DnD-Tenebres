package com.java_dragons.dnd_tenebres.domain.exploration.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "player_location_search_states", uniqueConstraints =
        @UniqueConstraint(name = "uq_player_location_search_state", columnNames = {"player_id", "location_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerLocationSearchState {

    public static final int MAX_ATTEMPTS = 10;
    public static final Duration REFRESH_INTERVAL = Duration.ofHours(2);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "location_id", nullable = false)
    private String locationId;

    @Column(name = "remaining_attempts", nullable = false)
    private int remainingAttempts;

    @Column(name = "refreshed_at", nullable = false)
    private Instant refreshedAt;

    public static PlayerLocationSearchState create(Long playerId, String locationId, Instant now) {
        return new PlayerLocationSearchState(null, null, playerId, locationId, MAX_ATTEMPTS, now);
    }

    public void refreshIfDue(Instant now) {
        if (now.isBefore(refreshedAt.plus(REFRESH_INTERVAL))) return;
        long intervals = Duration.between(refreshedAt, now).dividedBy(REFRESH_INTERVAL);
        remainingAttempts = MAX_ATTEMPTS;
        refreshedAt = refreshedAt.plus(REFRESH_INTERVAL.multipliedBy(intervals));
    }

    public int consume(Instant now) {
        refreshIfDue(now);
        if (remainingAttempts <= 0) {
            throw new IllegalStateException("Попытки поиска закончились. Они восстановятся через 2 часа");
        }
        return --remainingAttempts;
    }

    public int restore(Instant now, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Количество попыток не может быть отрицательным");
        refreshIfDue(now);
        remainingAttempts = Math.min(MAX_ATTEMPTS, Math.addExact(remainingAttempts, amount));
        return remainingAttempts;
    }
}
