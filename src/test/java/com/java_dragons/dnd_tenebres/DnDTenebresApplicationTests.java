package com.java_dragons.dnd_tenebres;

import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatActionRequest;
import com.java_dragons.dnd_tenebres.domain.combat.dto.CombatEvent;
import com.java_dragons.dnd_tenebres.domain.combat.model.CombatAction;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterReason;
import com.java_dragons.dnd_tenebres.domain.combat.model.EncounterStatus;
import com.java_dragons.dnd_tenebres.domain.combat.service.CombatEncounterService;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.effect.model.EffectType;
import com.java_dragons.dnd_tenebres.domain.item.model.DiceType;
import com.java_dragons.dnd_tenebres.domain.item.service.InventoryService;
import com.java_dragons.dnd_tenebres.domain.location.repository.LocationRepository;
import com.java_dragons.dnd_tenebres.domain.location.service.LocationClearService;
import com.java_dragons.dnd_tenebres.domain.location.service.RestingService;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.entity.PlayerStats;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestType;
import com.java_dragons.dnd_tenebres.domain.quest.repository.QuestTemplateRepository;
import com.java_dragons.dnd_tenebres.domain.quest.service.QuestService;
import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dnd_test;MODE=PostgreSQL;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true",
        "application.security.jwt.secret=тестовый-секрет-длиной-больше-тридцати-двух-байт"
})
@Transactional
class DnDTenebresApplicationTests {

    @Autowired private PlayerRepository players;
    @Autowired private UserAccountRepository accounts;
    @Autowired private LocationRepository locations;
    @Autowired private MonsterRepository monsters;
    @Autowired private InventoryService inventory;
    @Autowired private RestingService resting;
    @Autowired private CombatEncounterService combat;
    @Autowired private QuestTemplateRepository questTemplates;
    @Autowired private QuestService quests;
    @Autowired private LocationClearService clearedLocations;

    @Test
    void contextLoads() {
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void acceptsLocationQuestWithProgressFromPreviousClear(boolean alreadyCleared) {
        var player = createPlayer("city_adv_guild");
        String camp = "forest_goblin_camp";
        if (alreadyCleared) {
            clearedLocations.markLocationAsCleared(player.getId(), camp);
        }
        var template = questTemplates.findAllByOrderByIdAsc().stream()
                .filter(quest -> quest.getQuestType() == QuestType.CLEAR_LOCATION
                        && camp.equals(quest.getTargetIdentifier()))
                .findFirst().orElseThrow();

        var quest = quests.acceptQuestById(player.getId(), template.getId());

        assertThat(quest.canTurnIn()).isEqualTo(alreadyCleared);
        assertThat(quest.currentProgress()).isEqualTo(alreadyCleared ? template.getTargetCount() : 0);
    }

    @Test
    void shortRestSucceedsInThroneRoomWithoutRandomEncounters() {
        var player = createPlayer("crypt_boss_room");
        inventory.addItemToPlayer(player, "Припасы", 1);
        player.takeDamage(10);
        int hpBeforeRest = player.getCurrentHp();

        var rest = resting.takeShortRest(player.getId());

        assertThat(rest.isAmbushed()).isFalse();
        assertThat(player.getCurrentHp()).isGreaterThan(hpBeforeRest);
        assertThat(player.canTakeShortRest()).isFalse();
        assertThat(player.getInventory()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = EncounterReason.class, names = {"HUNT", "REST_AMBUSH"})
    void lethalReflectionFinishesEncounterAndGrantsRewardImmediately(EncounterReason reason) {
        var player = createPlayer("forest_goblin_camp");
        player.addEffect(new ActiveEffect(EffectType.THORNS, 2, 1));
        player.addEffect(new ActiveEffect(EffectType.STUN, 2, 1));
        var monster = monsters.save(Monster.builder()
                .name("Гоблин").templateName("Гоблин").level(100)
                .maxHp(1).currentHp(1).damageDice(DiceType.D4).damageBonus(3)
                .attackName("Удар").xpReward(1).goldReward(5).build());
        combat.startEncounter(player.getId(), List.of(monster), reason);

        var result = reason == EncounterReason.REST_AMBUSH
                ? combat.applyAmbushOpening(player.getId(), monster)
                : combat.executeAction(player.getId(), new CombatActionRequest(CombatAction.ATTACK,
                        monster.getId(), null, null));

        assertThat(result.status()).isEqualTo(EncounterStatus.VICTORY);
        assertThat(result.currentEnemy()).isNull();
        assertThat(result.remainingEnemies()).isZero();
        assertThat(result.events()).extracting(CombatEvent::actionType).containsSubsequence("REFLECT", "DEATH", "REWARD");
        assertThat(player.getGold()).isEqualTo(5);
    }

    private Player createPlayer(String locationId) {
        var account = accounts.save(UserAccount.create("test-hero", "test-password-hash"));
        return players.save(Player.builder()
                .name("Герой").accountId(account.getId())
                .stats(new PlayerStats(10, 10, 10, 10, 10, 10))
                .maxHp(20).currentHp(20).maxMp(10).currentMp(10)
                .currentLocation(locations.findById(locationId).orElseThrow())
                .build());
    }
}
