package com.java_dragons.dnd_tenebres.domain.combat.service;

import com.java_dragons.dnd_tenebres.core.config.GameBalanceProperties;
import com.java_dragons.dnd_tenebres.domain.combat.dto.*;
import com.java_dragons.dnd_tenebres.domain.combat.entity.*;
import com.java_dragons.dnd_tenebres.domain.combat.model.*;
import com.java_dragons.dnd_tenebres.domain.combat.repository.*;
import com.java_dragons.dnd_tenebres.domain.effect.model.ActiveEffect;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.repository.PlayerItemRepository;
import com.java_dragons.dnd_tenebres.domain.location.model.LocationEffect;
import com.java_dragons.dnd_tenebres.domain.monster.entity.Monster;
import com.java_dragons.dnd_tenebres.domain.monster.repository.MonsterRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CombatEncounterService {
    private final CombatEncounterRepository encounterRepository;
    private final CombatLogEntryRepository logRepository;
    private final PlayerRepository playerRepository;
    private final MonsterRepository monsterRepository;
    private final PlayerItemRepository playerItemRepository;
    private final SpellRepository spellRepository;
    private final CombatService combatService;
    private final GameBalanceProperties balance;
    private final SpellManaCostCalculator manaCostCalculator;

    @Transactional
    public CombatStateResponse startEncounter(Long playerId, List<Monster> monsters, EncounterReason reason) {
        if (monsters == null || monsters.isEmpty()) throw new IllegalArgumentException("Encounter requires enemies");
        if (encounterRepository.findForUpdate(playerId, EncounterStatus.ACTIVE).isPresent())
            throw new IllegalStateException("Player already has an active encounter");
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
        CombatEncounter encounter = CombatEncounter.start(player, Objects.requireNonNull(reason));
        for (int i = 0; i < monsters.size(); i++) {
            encounter.addParticipant(CombatParticipant.builder().encounter(encounter).monster(monsters.get(i))
                    .turnOrder(i).status(i == 0 ? ParticipantStatus.ACTIVE : ParticipantStatus.WAITING).build());
        }
        player.enterCombat(monsters.get(0).getId());
        encounterRepository.saveAndFlush(encounter);
        CombatEvent opening = new CombatEvent("SYSTEM", "ENCOUNTER_STARTED", monsters.get(0).getName(),
                monsters.size(), "Причина боя: " + reason);
        appendEvents(encounter, List.of(opening));
        return toState(encounter, List.of(opening));
    }

    @Transactional(readOnly = true)
    public CombatStateResponse current(Long playerId) {
        return toState(encounterRepository.findCurrent(playerId, EncounterStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active encounter")), List.of());
    }

    @Transactional(readOnly = true)
    public Page<CombatEvent> journal(Long playerId, Long encounterId, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, balance.maxCombatLogPageSize()));
        CombatEncounter encounter = encounterId == null
                ? encounterRepository.findFirstByPlayerIdOrderByCreatedAtDesc(playerId)
                    .orElseThrow(() -> new IllegalStateException("No encounters found"))
                : encounterRepository.findByIdAndPlayerId(encounterId, playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Encounter not found"));
        Page<CombatLogEntry> entries = logRepository.findByEncounterIdOrderByIdDesc(
                encounter.getId(), PageRequest.of(Math.max(0, page), safeSize));
        return entries.map(CombatLogEntry::toEvent);
    }

    @Transactional
    public CombatStateResponse executeAction(Long playerId, CombatActionRequest request) {
        CombatEncounter encounter = encounterRepository.findForUpdate(playerId, EncounterStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active encounter"));
        CombatParticipant current = encounter.currentParticipant()
                .orElseThrow(() -> new IllegalStateException("Encounter has no active enemy"));
        if (request.targetId() != null && !request.targetId().equals(current.getMonster().getId()))
            throw new IllegalArgumentException("Target does not belong to the active encounter");

        String selectedName = null;
        if (request.action() == CombatAction.CAST_SPELL) {
            if (request.abilityId() == null) throw new IllegalArgumentException("abilityId is required");
            Spell spell = spellRepository.findById(request.abilityId())
                    .orElseThrow(() -> new IllegalArgumentException("Ability not found"));
            validateSpell(encounter.getPlayer(), spell);
            selectedName = spell.getName();
        } else if (request.action() == CombatAction.USE_POTION) {
            if (request.itemId() == null) throw new IllegalArgumentException("itemId is required");
            PlayerItem item = playerItemRepository.findByIdAndPlayerId(request.itemId(), playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));
            if (item.getTemplate().getType() != ItemType.CONSUMABLE || item.getAmount() <= 0)
                throw new IllegalArgumentException("Item is not an available potion");
            selectedName = item.getId().toString();
        }

        int alive = (int) encounter.getParticipants().stream()
                .filter(p -> p.getStatus() == ParticipantStatus.ACTIVE || p.getStatus() == ParticipantStatus.WAITING).count();
        CombatReport report = combatService.executeTurn(encounter.getPlayer(), current.getMonster(), alive,
                encounter.getRound(), request.action(), selectedName);
        appendEvents(encounter, report.events());

        boolean fled = report.events().stream().anyMatch(e -> "FLEE_SUCCESS".equals(e.actionType()));
        if (report.isPlayerDead()) finish(encounter, EncounterStatus.DEFEAT);
        else if (fled) {
            current.markFled();
            finish(encounter, EncounterStatus.FLED);
        } else if (report.isEnemyDead()) {
            current.markDead();
            Optional<CombatParticipant> next = encounter.getParticipants().stream()
                    .filter(p -> p.getStatus() == ParticipantStatus.WAITING).findFirst();
            if (next.isPresent()) {
                next.get().activate();
                encounter.getPlayer().enterCombat(next.get().getMonster().getId());
                encounter.advanceRound();
            } else finish(encounter, EncounterStatus.VICTORY);
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
        if (encounter.getReason() != EncounterReason.REST_AMBUSH)
            throw new IllegalStateException("Encounter is not a rest ambush");
        CombatReport report = combatService.executeAmbushTurn(playerId, monster);
        appendEvents(encounter, report.events());
        if (report.isPlayerDead()) finish(encounter, EncounterStatus.DEFEAT);
        return toState(encounter, report.events());
    }

    private void validateSpell(Player player, Spell spell) {
        int maxTier = Math.min(5, 1 + Math.max(0, player.getLevel() - 1) / 4);
        if (spell.getTier() > maxTier) throw new IllegalStateException("Ability is not unlocked");
        var focus = player.getMainHandWeapon().filter(item -> item.getTemplate().getType() == ItemType.MAGIC_WEAPON)
                .orElseThrow(() -> new IllegalStateException("Для заклинания нужно магическое оружие"));
        if (spell.getTier() > focus.getTemplate().getRarity().getTierIndex())
            throw new IllegalStateException("Магическое оружие не поддерживает этот тир");
        if (player.getCurrentMp() < manaCostCalculator.calculate(player, spell, focus))
            throw new IllegalStateException("Not enough mana");
    }

    private void finish(CombatEncounter encounter, EncounterStatus status) {
        encounter.finish(status);
        encounter.getPlayer().leaveCombat();
    }

    private void appendEvents(CombatEncounter encounter, List<CombatEvent> events) {
        List<CombatLogEntry> entries = events.stream().map(event -> CombatLogEntry.builder()
                .encounter(encounter).round(encounter.getRound())
                .actor(Objects.requireNonNullElse(event.actor(), "SYSTEM"))
                .actionType(Objects.requireNonNullElse(event.actionType(), "INFO"))
                .target(Objects.requireNonNullElse(event.target(), ""))
                .value(event.value()).description(Objects.requireNonNullElse(event.description(), ""))
                .createdAt(Instant.now()).build()).toList();
        logRepository.saveAll(entries);
    }

    private CombatStateResponse toState(CombatEncounter encounter, List<CombatEvent> events) {
        Player player = encounter.getPlayer();
        CombatStateResponse.EnemyState enemy = encounter.currentParticipant().map(cp -> {
            Monster monster = cp.getMonster();
            return new CombatStateResponse.EnemyState(monster.getId(), monster.getName(), monster.getLevel(),
                    monster.getCurrentHp(), monster.getMaxHp(),
                    monster.getElements().stream().map(Enum::name).sorted().toList(),
                    "monster:" + monster.getTemplateName(), effects(monster.getCombatEffects()));
        }).orElse(null);
        long remaining = encounter.getParticipants().stream()
                .filter(cp -> cp.getStatus() == ParticipantStatus.ACTIVE || cp.getStatus() == ParticipantStatus.WAITING).count();
        int maxTier = Math.min(5, 1 + Math.max(0, player.getLevel() - 1) / 4);
        var magicFocus = player.getMainHandWeapon().filter(item -> item.getTemplate().getType() == ItemType.MAGIC_WEAPON);
        List<CombatStateResponse.AbilityState> abilities = spellRepository
                .findByTierLessThanEqualOrderByTierAscNameAsc(maxTier).stream().map(spell -> {
                    boolean supported = magicFocus.isPresent() && spell.getTier() <= magicFocus.get().getTemplate().getRarity().getTierIndex();
                    int manaCost = magicFocus.map(focus -> manaCostCalculator.calculate(player, spell, focus))
                            .orElse(spell.getManaCost());
                    boolean available = supported && player.getCurrentMp() >= manaCost;
                    String reason = available ? null : !supported ? "MAGIC_FOCUS_REQUIRED" : "NOT_ENOUGH_MANA";
                    return new CombatStateResponse.AbilityState(spell.getId(), spell.getName(), spell.getTier(),
                            manaCost, spell.getElement().name(), available, reason);
                }).toList();
        List<CombatStateResponse.PotionState> potions = playerItemRepository.findByPlayerId(player.getId()).stream()
                .filter(item -> item.getTemplate().getType() == ItemType.CONSUMABLE && item.getAmount() > 0)
                .map(item -> new CombatStateResponse.PotionState(item.getId(), item.getTemplate().getId(),
                        item.getTemplate().getName(), item.getAmount(),
                        item.getTemplate().getConsumableAction().name(), true)).toList();
        List<CombatEvent> journal = new ArrayList<>(logRepository.findTop100ByEncounterIdOrderByIdDesc(encounter.getId())
                .stream().map(CombatLogEntry::toEvent).toList());
        Collections.reverse(journal);
        List<CombatAction> actions = encounter.getStatus() == EncounterStatus.ACTIVE
                ? List.of(CombatAction.ATTACK, CombatAction.CAST_SPELL, CombatAction.USE_POTION, CombatAction.FLEE)
                : List.of();
        return new CombatStateResponse(encounter.getId(), encounter.getStatus(), encounter.getReason(), encounter.getRound(),
                new CombatStateResponse.PlayerState(player.getId(), player.getName(), player.getLevel(),
                        player.getCurrentHp(), player.getMaxHp(), player.getCurrentMp(), player.getMaxMp(),
                        "hero:default", effects(player.getActiveEffects())), enemy, remaining, actions,
                abilities, potions, events, journal, encounter.getCreatedAt());
    }

    private List<CombatStateResponse.EffectState> effects(Collection<ActiveEffect> effects) {
        return effects.stream().map(effect -> new CombatStateResponse.EffectState(effect.getType().name(),
                effect.getDuration(), effect.getPower(), effect.getType().getEffectCategory().name())).toList();
    }
}
