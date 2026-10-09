package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.core.math.ItemProgressionCalculator;
import com.java_dragons.dnd_tenebres.domain.item.dto.ItemUpgradePreviewResponse;
import com.java_dragons.dnd_tenebres.domain.item.entity.ForgeOperation;
import com.java_dragons.dnd_tenebres.domain.item.entity.PlayerItem;
import com.java_dragons.dnd_tenebres.domain.item.model.ItemType;
import com.java_dragons.dnd_tenebres.domain.item.repository.ForgeOperationRepository;
import com.java_dragons.dnd_tenebres.domain.item.repository.PlayerItemRepository;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemUpgradeService {

    private final PlayerItemRepository playerItemRepository;
    private final PlayerRepository playerRepository;
    private final ForgeOperationRepository forgeOperationRepository;
    private final ItemProgressionCalculator itemProgressionCalculator;
    private final com.java_dragons.dnd_tenebres.domain.combat.service.CombatStateService combatStateService;

    @Transactional
    public ItemUpgradePreviewResponse feedItems(
            Long playerId,
            String operationId,
            Long targetItemId,
            List<Long> foodItemIds
    ) {
        validateRequest(operationId, targetItemId, foodItemIds);
        String normalizedOperationId = operationId.trim();
        String requestHash = requestHash(targetItemId, foodItemIds);

        var player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Игрок не найден"));
        combatStateService.requireOutOfCombat(playerId, "Нельзя улучшать предметы во время боя");

        var previous = forgeOperationRepository.findByPlayerIdAndOperationId(playerId, normalizedOperationId);
        if (previous.isPresent()) {
            ForgeOperation operation = previous.get();
            if (!operation.getTargetItemId().equals(targetItemId)
                    || !operation.getRequestHash().equals(requestHash)) {
                throw new IllegalStateException("operationId уже использован для другого запроса кузницы");
            }
            return savedResult(operation);
        }

        PlayerItem target = playerItemRepository.findByIdAndPlayerId(targetItemId, playerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Улучшаемый предмет не найден"));

        ItemType targetType = target.getTemplate().getType();
        if (targetType == ItemType.ARTIFACT || targetType == ItemType.CONSUMABLE || targetType == ItemType.RESOURCE) {
            throw new IllegalArgumentException("Этот тип предмета нельзя прокачивать");
        }
        if (foodItemIds.contains(targetItemId)) {
            throw new IllegalArgumentException("Нельзя скармливать предмет самому себе");
        }

        List<PlayerItem> foodItems = playerItemRepository.findAllById(foodItemIds);
        if (foodItems.size() != foodItemIds.size()) {
            throw new jakarta.persistence.EntityNotFoundException("Один или несколько предметов для поглощения не найдены");
        }
        if (foodItems.stream().anyMatch(item -> !item.getPlayer().getId().equals(playerId))) {
            throw new IllegalArgumentException("Один из предметов для поглощения принадлежит другому игроку");
        }
        if (foodItems.stream().anyMatch(PlayerItem::isEquipped)) {
            throw new IllegalStateException("Сначала снимите все предметы, выбранные для поглощения");
        }
        if (foodItems.stream().anyMatch(PlayerItem::isLocked)) {
            throw new IllegalStateException("Заблокированный предмет нельзя использовать для поглощения");
        }

        long currentXp = target.getItemXp();
        int currentTier = target.getTier();
        int gainedXp = 0;
        for (PlayerItem foodItem : foodItems) {
            int xpYield = itemProgressionCalculator.calculateXpYield(
                    target.getTemplate().getType(), target.getTemplate().getRarity(),
                    foodItem.getTemplate().getType(), foodItem.getTemplate().getRarity()
            );
            gainedXp = Math.addExact(gainedXp, xpYield);
        }

        long resultingXp = Math.addExact(currentXp, gainedXp);
        int resultingTier = itemProgressionCalculator.getTierByXp(resultingXp);
        target.addXp(gainedXp, resultingTier);
        playerItemRepository.deleteAll(foodItems);

        forgeOperationRepository.save(ForgeOperation.builder()
                .playerId(playerId)
                .operationId(normalizedOperationId)
                .requestHash(requestHash)
                .targetItemId(targetItemId)
                .gainedXp(gainedXp)
                .resultingXp(resultingXp)
                .resultingTier(resultingTier)
                .createdAt(Instant.now())
                .build());

        return new ItemUpgradePreviewResponse(
                targetItemId,
                currentXp,
                gainedXp,
                resultingXp,
                currentTier,
                resultingTier,
                itemProgressionCalculator.getRequiredHeroLevelForTier(resultingTier),
                List.copyOf(foodItemIds),
                List.of(),
                false
        );
    }

    private ItemUpgradePreviewResponse savedResult(ForgeOperation operation) {
        long currentXp = operation.getResultingXp() - operation.getGainedXp();
        int currentTier = itemProgressionCalculator.getTierByXp(currentXp);
        return new ItemUpgradePreviewResponse(
                operation.getTargetItemId(),
                currentXp,
                operation.getGainedXp(),
                operation.getResultingXp(),
                currentTier,
                operation.getResultingTier(),
                itemProgressionCalculator.getRequiredHeroLevelForTier(operation.getResultingTier()),
                List.of(),
                List.of("Операция уже была выполнена; возвращён сохранённый результат"),
                true
        );
    }

    private void validateRequest(String operationId, Long targetItemId, List<Long> foodItemIds) {
        if (operationId == null || operationId.isBlank() || operationId.length() > 100) {
            throw new IllegalArgumentException("operationId обязателен и не может быть длиннее 100 символов");
        }
        if (targetItemId == null) {
            throw new IllegalArgumentException("ID улучшаемого предмета обязателен");
        }
        if (foodItemIds == null || foodItemIds.isEmpty() || foodItemIds.size() > 100) {
            throw new IllegalArgumentException("Нужно выбрать от 1 до 100 предметов для поглощения");
        }
        if (foodItemIds.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("ID предмета для поглощения не может быть пустым");
        }
        if (new HashSet<>(foodItemIds).size() != foodItemIds.size()) {
            throw new IllegalArgumentException("Список предметов для поглощения содержит повторяющиеся ID");
        }
    }

    private String requestHash(Long targetItemId, List<Long> foodItemIds) {
        List<Long> sortedFoodIds = new ArrayList<>(foodItemIds);
        sortedFoodIds.sort(Long::compareTo);
        String canonicalRequest = targetItemId + ":" + sortedFoodIds;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 недоступен", exception);
        }
    }
}
