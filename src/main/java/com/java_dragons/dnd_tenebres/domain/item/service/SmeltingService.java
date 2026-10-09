package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.domain.combat.service.CombatStateService;
import com.java_dragons.dnd_tenebres.domain.item.dto.SmeltResponse;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SmeltingService {
    public static final int ORE_PER_INGOT = 2;
    public static final int WOOD_PER_INGOT = 1;
    private static final Map<String, String> RECIPES = Map.of(
            "Железная руда", "Железный слиток",
            "Мифриловая руда", "Мифриловый слиток",
            "Орихалковая руда", "Орихалковый слиток");
    private final PlayerRepository playerRepository;
    private final InventoryService inventoryService;
    private final CombatStateService combatStateService;

    @Transactional
    public SmeltResponse smelt(Long playerId, String oreName, int amount) {
        if (amount < 1 || amount > 1000) throw new IllegalArgumentException("Количество слитков: от 1 до 1000");
        String ingotName = oreName == null ? null : RECIPES.get(oreName);
        if (ingotName == null) throw new IllegalArgumentException("Неизвестный рецепт переплавки");
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new EntityNotFoundException("Игрок не найден"));
        combatStateService.requireOutOfCombat(playerId, "Нельзя переплавлять руду во время боя");
        if (player.getCurrentLocation() == null || !"city_forge".equals(player.getCurrentLocation().getId())) {
            throw new IllegalStateException("Переплавка доступна только в городской кузнице");
        }
        int ore = Math.multiplyExact(amount, ORE_PER_INGOT);
        int wood = Math.multiplyExact(amount, WOOD_PER_INGOT);
        List<PlayerItem> oreStacks = usableStacks(player, oreName);
        List<PlayerItem> woodStacks = usableStacks(player, "Древесина");
        requireAmount(oreStacks, ore, oreName);
        requireAmount(woodStacks, wood, "Древесина");
        consume(player, oreStacks, ore);
        consume(player, woodStacks, wood);
        inventoryService.addItemToPlayer(player, ingotName, amount);
        return new SmeltResponse(oreName, ingotName, amount, ore, wood);
    }

    private List<PlayerItem> usableStacks(Player player, String name) {
        return player.getInventory().stream().filter(item -> item.getTemplate().getType() == ItemType.RESOURCE)
                .filter(item -> name.equals(item.getTemplate().getName()))
                .filter(item -> !item.isLocked() && !item.isEquipped()).toList();
    }

    private void requireAmount(List<PlayerItem> stacks, int needed, String name) {
        if (stacks.stream().mapToLong(PlayerItem::getAmount).sum() < needed)
            throw new IllegalStateException("Недостаточно незаблокированных ресурсов: " + name);
    }

    private void consume(Player player, List<PlayerItem> stacks, int remaining) {
        for (PlayerItem stack : stacks) {
            int used = Math.min(remaining, stack.getAmount());
            if (used == stack.getAmount()) player.getInventory().remove(stack);
            else stack.setAmount(stack.getAmount() - used);
            remaining -= used;
            if (remaining == 0) break;
        }
    }
}
