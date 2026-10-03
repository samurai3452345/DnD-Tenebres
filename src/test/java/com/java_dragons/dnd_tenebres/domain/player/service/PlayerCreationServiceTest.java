package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerCreationServiceTest {

    @Test
    void связываетНовогоПерсонажаСАккаунтом() {
        ProgressionCalculator progression = mock(ProgressionCalculator.class);
        when(progression.getHeroBaseHp(1)).thenReturn(10);
        when(progression.calculateMaxMp(1, 8)).thenReturn(4);
        PlayerCreationService service = new PlayerCreationService(progression);

        var player = service.createCharacter(
                7L,
                new PlayerCreationRequest(" Герой ", 15, 15, 15, 8, 8, 8)
        );

        assertThat(player.getAccountId()).isEqualTo(7L);
        assertThat(player.getName()).isEqualTo("Герой");
    }
}
