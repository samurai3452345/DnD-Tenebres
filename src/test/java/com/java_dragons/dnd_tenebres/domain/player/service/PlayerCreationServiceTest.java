package com.java_dragons.dnd_tenebres.domain.player.service;

import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculator;
import com.java_dragons.dnd_tenebres.core.math.ProgressionCalculatorImpl;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerCreationServiceTest {

    @Test
    void связываетНовогоПерсонажаСАккаунтом() {
        ProgressionCalculator progression = new ProgressionCalculatorImpl();
        PlayerCreationService service = new PlayerCreationService(progression);

        var player = service.createCharacter(
                7L,
                new PlayerCreationRequest(" Герой ", 15, 15, 15, 8, 8, 8)
        );

        assertThat(player.getAccountId()).isEqualTo(7L);
        assertThat(player.getName()).isEqualTo("Герой");
        assertThat(player.getCurrentMp()).isEqualTo(17);
        assertThat(player.getMaxMp()).isEqualTo(17);

        player.levelUp(progression.getHeroBaseHp(2), progression.getHeroBaseMp(2));

        assertThat(player.getCurrentMp()).isEqualTo(24);
        assertThat(player.getMaxMp()).isEqualTo(24);
    }
}
