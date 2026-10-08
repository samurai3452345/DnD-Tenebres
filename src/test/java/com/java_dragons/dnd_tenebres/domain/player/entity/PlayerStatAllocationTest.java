package com.java_dragons.dnd_tenebres.domain.player.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerStatAllocationTest {

    @Test
    void rejectsIntegerOverflowExploitWithoutSpendingPoint() {
        Player player = playerWithStats(1, 8);

        assertThatThrownBy(() -> player.allocateStats(
                1_073_741_824, 1_073_741_824, 1_073_741_824, 1_073_741_824, 1, 0
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Недостаточно поинтов");

        assertThat(player.getStatPoints()).isEqualTo(1);
        assertThat(player.getStats().getStrength()).isEqualTo(8);
    }

    @Test
    void rejectsIncreasePastBaseStatCapAtomically() {
        Player player = playerWithStats(2, PlayerStats.MAX_BASE_STAT);

        assertThatThrownBy(() -> player.allocateStats(1, 1, 0, 0, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("не может превышать");

        assertThat(player.getStatPoints()).isEqualTo(2);
        assertThat(player.getStats().getStrength()).isEqualTo(PlayerStats.MAX_BASE_STAT);
        assertThat(player.getStats().getDexterity()).isEqualTo(PlayerStats.MAX_BASE_STAT);
    }

    @Test
    void rejectsMixedNegativeAllocationAtDomainBoundary() {
        Player player = playerWithStats(1, 8);

        assertThatThrownBy(() -> player.allocateStats(-1, 1, 0, 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Player playerWithStats(int points, int statValue) {
        return Player.builder()
                .statPoints(points)
                .stats(PlayerStats.builder()
                        .strength(statValue)
                        .dexterity(statValue)
                        .constitution(statValue)
                        .intelligence(statValue)
                        .wisdom(statValue)
                        .charisma(statValue)
                        .build())
                .build();
    }
}
