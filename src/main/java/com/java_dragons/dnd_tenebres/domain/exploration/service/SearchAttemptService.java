package com.java_dragons.dnd_tenebres.domain.exploration.service;

import com.java_dragons.dnd_tenebres.domain.exploration.entity.PlayerLocationSearchState;
import com.java_dragons.dnd_tenebres.domain.exploration.repository.PlayerLocationSearchStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;

@Service
public class SearchAttemptService {

    private final PlayerLocationSearchStateRepository repository;
    private final Clock clock;

    @Autowired
    public SearchAttemptService(PlayerLocationSearchStateRepository repository) {
        this(repository, Clock.systemUTC());
    }

    SearchAttemptService(PlayerLocationSearchStateRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public int consume(Long playerId, String locationId) {
        PlayerLocationSearchState state = state(playerId, locationId);
        return state.consume(Instant.now(clock));
    }

    public int restore(Long playerId, String locationId, int amount) {
        PlayerLocationSearchState state = state(playerId, locationId);
        return state.restore(Instant.now(clock), amount);
    }

    private PlayerLocationSearchState state(Long playerId, String locationId) {
        return repository.findByPlayerIdAndLocationId(playerId, locationId)
                .orElseGet(() -> repository.save(PlayerLocationSearchState.create(
                        playerId, locationId, Instant.now(clock))));
    }
}
