package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.domain.combat.dto.*;
import com.java_dragons.dnd_tenebres.domain.combat.entity.*;
import com.java_dragons.dnd_tenebres.domain.combat.model.*;
import com.java_dragons.dnd_tenebres.domain.combat.repository.*;
import com.java_dragons.dnd_tenebres.domain.item.repository.PlayerItemRepository;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CombatEncounterService {
    private final CombatEncounterRepository encounterRepository;
    private final PlayerRepository playerRepository;
    private final MonsterRepository monsterRepository;
    private final PlayerItemRepository playerItemRepository;
    private final SpellRepository spellRepository;
    private final CombatService combatService;

    @Transactional
    public CombatStateResponse startEncounter(Long playerId, List<Monster> monsters, EncounterReason reason) {
        if (monsters == null || monsters.isEmpty()) throw new IllegalArgumentException("Encounter requires enemies");
        if (encounterRepository.findForUpdate(playerId, EncounterStatus.ACTIVE).isPresent())
            throw new IllegalStateException("Player already has an active encounter");
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
        CombatEncounter encounter = CombatEncounter.start(player, reason);
        for (int i = 0; i < monsters.size(); i++) {
            encounter.addParticipant(CombatParticipant.builder().encounter(encounter).monster(monsters.get(i))
                    .turnOrder(i).status(i == 0 ? ParticipantStatus.ACTIVE : ParticipantStatus.WAITING).build());
        }
        player.enterCombat(monsters.get(0).getId());
        return toState(encounterRepository.save(encounter), List.of());
    }

    @Transactional(readOnly = true)
    public CombatStateResponse current(Long playerId) {
        return toState(encounterRepository.findCurrent(playerId, EncounterStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active encounter")), List.of());
    }

    @Transactional
    public CombatStateResponse executeAction(Long playerId, CombatActionRequest request) {
        CombatEncounter encounter = encounterRepository.findForUpdate(playerId, EncounterStatus.ACTIVE)
                .orElseGet(() -> bootstrapLegacyEncounter(playerId));
        CombatParticipant current = encounter.currentParticipant()
                .orElseThrow(() -> new IllegalStateException("Encounter has no active enemy"));
        if (request.targetId() != null && !request.targetId().equals(current.getMonster().getId()))
            throw new IllegalArgumentException("Target does not belong to the active encounter");

        String targetName = null;
        if (request.action() == CombatAction.CAST_SPELL) {
            if (request.abilityId() == null) throw new IllegalArgumentException("abilityId is required");
            targetName = spellRepository.findById(request.abilityId()).orElseThrow(() -> new IllegalArgumentException("Ability not found")).getName();
        } else if (request.action() == CombatAction.USE_POTION) {
            if (request.itemId() == null) throw new IllegalArgumentException("itemId is required");
            targetName = playerItemRepository.findByIdAndPlayerId(request.itemId(), playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found")).getTemplate().getName();
        }

        int alive = (int) encounter.getParticipants().stream().filter(p -> p.getStatus() != ParticipantStatus.DEAD && p.getStatus() != ParticipantStatus.FLED).count();
        CombatReport report = combatService.executeTurn(encounter.getPlayer(), current.getMonster(), alive,
                encounter.getRound(), request.action(), targetName);

        boolean fled = report.events().stream().anyMatch(e -> "FLEE_SUCCESS".equals(e.actionType()));
        if (report.isPlayerDead()) encounter.finish(EncounterStatus.DEFEAT);
        else if (fled) {
            current.markFled();
            encounter.finish(EncounterStatus.FLED);
        } else if (report.isEnemyDead()) {
            current.markDead();
            Optional<CombatParticipant> next = encounter.getParticipants().stream().filter(p -> p.getStatus() == ParticipantStatus.WAITING).findFirst();
            if (next.isPresent()) {
                next.get().activate();
                encounter.getPlayer().enterCombat(next.get().getMonster().getId());
                encounter.advanceRound();
            } else encounter.finish(EncounterStatus.VICTORY);
        } else encounter.advanceRound();
        return toState(encounter, report.events());
    }

    @Transactional
    public CombatStateResponse flee(Long playerId) {
        return executeAction(playerId, new CombatActionRequest(CombatAction.FLEE, null, null, null));
    }

    @Transactional
    public CombatStateResponse applyAmbushOpening(Long playerId, Monster monster) {
        CombatEncounter encounter = encounterRepository.findForUpdate(playerId, EncounterStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active ambush encounter"));
        CombatReport report = combatService.executeAmbushTurn(playerId, monster);
        if (report.isPlayerDead()) encounter.finish(EncounterStatus.DEFEAT);
        return toState(encounter, report.events());
    }

    private CombatEncounter bootstrapLegacyEncounter(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
        if (!player.isInCombat()) throw new IllegalStateException("No active encounter");
        Monster monster = monsterRepository.findById(player.getActiveCombatMonsterId()).orElseThrow(() -> new IllegalStateException("Active enemy is missing"));
        CombatEncounter encounter = CombatEncounter.start(player, EncounterReason.SCRIPTED);
        encounter.addParticipant(CombatParticipant.builder().encounter(encounter).monster(monster).turnOrder(0).status(ParticipantStatus.ACTIVE).build());
        return encounterRepository.save(encounter);
    }

    private CombatStateResponse toState(CombatEncounter e, List<CombatEvent> events) {
        Player p = e.getPlayer();
        CombatStateResponse.EnemyState enemy = e.currentParticipant().map(cp -> new CombatStateResponse.EnemyState(
                cp.getMonster().getId(), cp.getMonster().getName(), cp.getMonster().getCurrentHp(), cp.getMonster().getMaxHp())).orElse(null);
        long remaining = e.getParticipants().stream().filter(cp -> cp.getStatus() == ParticipantStatus.ACTIVE || cp.getStatus() == ParticipantStatus.WAITING).count();
        return new CombatStateResponse(e.getId(), e.getStatus(), e.getRound(),
                new CombatStateResponse.PlayerState(p.getCurrentHp(), p.getMaxHp(), p.getCurrentMp(), p.getMaxMp()), enemy, remaining, events);
    }
}
