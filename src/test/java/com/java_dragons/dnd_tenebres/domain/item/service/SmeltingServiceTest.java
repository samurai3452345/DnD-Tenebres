package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.combat.service.CombatStateService;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmeltingServiceTest {
    private final PlayerRepository repository = mock(PlayerRepository.class);
    private final InventoryService inventory = mock(InventoryService.class);
    private final CombatStateService combat = mock(CombatStateService.class);
    private final SmeltingService service = new SmeltingService(repository, inventory, combat);
    private Player player;

    @BeforeEach void setup() {
        Location location = new Location(); location.setId("city_forge");
        player = Player.builder().id(1L).currentLocation(location).build();
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
    }

    @ParameterizedTest
    @CsvSource({"Железная руда,Железный слиток", "Мифриловая руда,Мифриловый слиток", "Орихалковая руда,Орихалковый слиток"})
    void consumesAcrossStacksAndProducesCorrectIngots(String ore, String ingot) {
        PlayerItem first = stack(ore, 3), second = stack(ore, 4), wood = stack("Древесина", 3);
        var response = service.smelt(1L, ore, 3);
        assertThat(player.getInventory()).containsExactly(second);
        assertThat(second.getAmount()).isEqualTo(1);
        assertThat(response.oreConsumed()).isEqualTo(6);
        assertThat(response.woodConsumed()).isEqualTo(3);
        verify(inventory).addItemToPlayer(player, ingot, 3);
    }

    @Test void missingFuelDoesNotConsumeOre() {
        PlayerItem ore = stack("Железная руда", 10);
        assertThatThrownBy(() -> service.smelt(1L, "Железная руда", 1)).isInstanceOf(IllegalStateException.class);
        assertThat(ore.getAmount()).isEqualTo(10);
        verifyNoInteractions(inventory);
    }

    @Test void lockedResourcesCannotBeConsumed() {
        stack("Железная руда", 10).setLocked(true); stack("Древесина", 10);
        assertThatThrownBy(() -> service.smelt(1L, "Железная руда", 1)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(inventory);
    }

    @Test void outsideForgeIsRejected() {
        player.getCurrentLocation().setId("forest_edge");
        assertThatThrownBy(() -> service.smelt(1L, "Железная руда", 1)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(inventory);
    }

    @Test void combatIsRejected() {
        doThrow(new IllegalStateException("combat")).when(combat).requireOutOfCombat(eq(1L), anyString());
        assertThatThrownBy(() -> service.smelt(1L, "Железная руда", 1)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(inventory);
    }

    @Test void invalidRequestsAreRejectedBeforeLoadingPlayer() {
        for (int amount : new int[]{0, -1, 1001, Integer.MAX_VALUE})
            assertThatThrownBy(() -> service.smelt(1L, "Железная руда", amount)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.smelt(1L, "Кожа", 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.smelt(1L, null, 1)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(repository, inventory);
    }

    private PlayerItem stack(String name, int amount) {
        ItemTemplate template = new ItemTemplate();
        ReflectionTestUtils.setField(template, "name", name);
        ReflectionTestUtils.setField(template, "type", ItemType.RESOURCE);
        PlayerItem item = PlayerItem.builder().player(player).template(template).amount(amount).build();
        player.getInventory().add(item);
        return item;
    }
}
