package com.java_dragons.dnd_tenebres.domain.exploration.service;

import com.java_dragons.dnd_tenebres.core.random.RandomSource;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatStateService;
import com.java_dragons.dnd_tenebres.domain.exploration.model.ExplorationEventType;
import com.java_dragons.dnd_tenebres.domain.item.entity.ItemTemplate;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.entity.Location;
import com.java_dragons.dnd_tenebres.domain.location.entity.LocationLootEntry;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationType;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationLootEntryRepository;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationEffectService;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExplorationSearchTest {
    @Mock PlayerRepository players;
    @Mock InventoryService inventory;
    @Mock RandomSource random;
    @Mock LocationEffectService effects;
    @Mock SearchAttemptService attempts;
    @Mock CombatStateService combat;
    @Mock LocationLootEntryRepository loot;
    @Mock ApplicationEventPublisher events;
    @InjectMocks ExplorationService service;
    private AutoCloseable mocks;
    private Player player;
    private Location location;

    @BeforeEach void setup() {
        mocks = MockitoAnnotations.openMocks(this);
        location = new Location(); location.setId("forest_edge"); location.setSearchDifficulty(6);
        player = Player.builder().id(1L).currentLocation(location).build();
        when(players.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
        when(attempts.consume(eq(1L), anyString())).thenReturn(9);
        when(effects.searchDifficulty(eq(player), anyInt())).thenAnswer(call -> call.getArgument(1));
    }
    @AfterEach void cleanup() throws Exception { mocks.close(); }

    @Test void fiveFailsAndStillConsumesAttempt() {
        when(random.roll(1, 20)).thenReturn(5);
        assertThat(service.search(1L).eventType()).isEqualTo(ExplorationEventType.NOTHING_FOUND);
        verify(attempts).consume(1L, "forest_edge");
        verifyNoInteractions(inventory, loot);
    }

    @ParameterizedTest @ValueSource(strings = {"forest_edge", "forest_wolf_trail", "forest_goblin_camp"})
    void sixSucceedsInAllForestLocations(String id) {
        location.setId(id); when(random.roll(1, 20)).thenReturn(6);
        stubLoot();
        assertThat(service.search(1L).eventType()).isEqualTo(ExplorationEventType.FOUND_LOOT);
        verify(inventory).addItemToPlayer(player, "Железная руда", 2);
        verify(attempts).consume(1L, id);
    }

    @Test void dangerousDungeonCanBeSearchedOutsideCombat() {
        location.setId("crypt_armory"); location.setType(LocationType.DANGEROUS); location.setSearchDifficulty(14);
        when(random.roll(1, 20)).thenReturn(14); stubLoot();
        assertThat(service.search(1L).eventType()).isEqualTo(ExplorationEventType.FOUND_LOOT);
    }

    @Test void combatDoesNotConsumeAttempt() {
        when(combat.isInCombat(1L)).thenReturn(true);
        assertThatThrownBy(() -> service.search(1L)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(attempts, random, inventory);
    }

    private void stubLoot() {
        ItemTemplate template = new ItemTemplate(); ReflectionTestUtils.setField(template, "name", "Железная руда");
        when(loot.findByLocationId(location.getId())).thenReturn(List.of(LocationLootEntry.builder()
                .itemTemplate(template).minAmount(1).maxAmount(2).findChance(30).build()));
        when(random.chance(30)).thenReturn(true); when(random.nextInt(1, 3)).thenReturn(2);
    }
}
