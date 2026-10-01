package com.java_dragons.dnd_tenebres.domain.item.service;

import com.java_dragons.dnd_tenebres.core.event.QuestProgressEvent;
import com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason;
import com.java_dragons.dnd_tenebres.domain.economy.service.WalletService;
import com.java_dragons.dnd_tenebres.domain.item.dto.*;
import com.java_dragons.dnd_tenebres.domain.item.entity.*;
import com.java_dragons.dnd_tenebres.domain.item.repository.*;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import com.java_dragons.dnd_tenebres.domain.quest.model.QuestType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor
public class ShopService {
    public static final String MERCHANT_LOCATION_ID = "city_merch_guild";
    private final PlayerRepository playerRepository;
    private final PlayerItemRepository playerItemRepository;
    private final MerchantOfferRepository offerRepository;
    private final TradeOperationRepository operationRepository;
    private final InventoryService inventoryService;
    private final WalletService walletService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.java_dragons.dnd_tenebres.infrastructure.audit.AuditService auditService;

    @Transactional(readOnly = true)
    public MerchantResponse currentMerchant(Long playerId) {
        Player player = player(playerId); validateLocation(player);
        return new MerchantResponse("TALMIRIA_GENERAL", "Торговец Гильдии", MERCHANT_LOCATION_ID);
    }

    @Transactional(readOnly = true)
    public List<ShopOfferResponse> getAssortment(Long playerId) {
        Player player = player(playerId); validateLocation(player);
        return offerRepository.findByLocationIdAndEnabledTrueOrderById(player.getCurrentLocation().getId()).stream()
                .map(offer -> new ShopOfferResponse(offer.getId(), offer.getTemplate().getId(), offer.getTemplate().getName(),
                        offer.getTemplate().getType().name(), offer.getTemplate().getRarity().name(), offer.getUnitPrice(),
                        offer.getAvailableQuantity(), offer.getMinLevel(), player.getLevel() >= offer.getMinLevel() &&
                        (offer.getAvailableQuantity() < 0 || offer.getAvailableQuantity() > 0))).toList();
    }

    @Transactional
    public String buyItem(Long playerId, String operationId, Long offerId, int amount) {
        validateOperation(operationId, amount);
        Optional<TradeOperation> old = operationRepository.findByPlayerIdAndOperationId(playerId, operationId);
        if (old.isPresent()) return old.get().getResult();
        Player player = player(playerId); validateLocation(player);
        MerchantOffer offer = offerRepository.findByIdAndLocationIdAndEnabledTrue(offerId, player.getCurrentLocation().getId())
                .orElseThrow(() -> new IllegalArgumentException("Товар отсутствует у этого торговца"));
        if (player.getLevel() < offer.getMinLevel()) throw new IllegalStateException("Требуется уровень " + offer.getMinLevel());
        long price = Math.multiplyExact((long) offer.getUnitPrice(), amount);
        walletService.debit(player, price, WalletReason.PURCHASE, "MERCHANT_OFFER", offerId.toString());
        offer.take(amount);
        inventoryService.addItemToPlayer(player, offer.getTemplate().getName(), amount);
        String result = "Куплено: " + offer.getTemplate().getName() + " x" + amount + " за " + price;
        remember(playerId, operationId, "BUY", result);
        auditService.record(player.getName(), playerId, "PURCHASE", "SUCCESS", result);
        eventPublisher.publishEvent(new QuestProgressEvent(playerId, QuestType.TRADE, "BUY", amount));
        return result;
    }

    @Transactional
    public String sellItem(Long playerId, String operationId, Long playerItemId, int amount) {
        validateOperation(operationId, amount);
        Optional<TradeOperation> old = operationRepository.findByPlayerIdAndOperationId(playerId, operationId);
        if (old.isPresent()) return old.get().getResult();
        Player player = player(playerId); validateLocation(player);
        PlayerItem item = playerItemRepository.findByIdAndPlayerId(playerItemId, playerId)
                .orElseThrow(() -> new IllegalArgumentException("Предмет не найден"));
        if (item.isEquipped()) throw new IllegalStateException("Сначала снимите предмет");
        if (item.isLocked()) throw new IllegalStateException("Заблокированный предмет нельзя продать");
        if (item.getAmount() < amount) throw new IllegalArgumentException("Недостаточное количество");
        int unitPrice = Math.max(1, item.getTemplate().getRarity().getTierIndex() * 10 + item.getTemplate().getStatBudget() / 2);
        long price = Math.multiplyExact((long) unitPrice, amount);
        walletService.credit(player, price, WalletReason.SALE, "PLAYER_ITEM", playerItemId.toString());
        item.setAmount(item.getAmount() - amount);
        if (item.getAmount() == 0) { player.getInventory().remove(item); playerItemRepository.delete(item); }
        String result = "Продано: " + item.getTemplate().getName() + " x" + amount + " за " + price;
        remember(playerId, operationId, "SELL", result);
        auditService.record(player.getName(), playerId, "SALE", "SUCCESS", result);
        eventPublisher.publishEvent(new QuestProgressEvent(playerId, QuestType.TRADE, "SELL", amount));
        return result;
    }

    private Player player(Long id) { return playerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Игрок не найден")); }
    private void validateLocation(Player p) {
        if (p.isInCombat()) throw new IllegalStateException("Нельзя торговать во время боя");
        if (p.getCurrentLocation() == null || !MERCHANT_LOCATION_ID.equals(p.getCurrentLocation().getId()))
            throw new IllegalStateException("Торговец недоступен в текущей локации");
    }
    private void validateOperation(String id, int amount) {
        if (id == null || id.isBlank() || id.length() > 100) throw new IllegalArgumentException("operationId is required");
        if (amount <= 0 || amount > 1000) throw new IllegalArgumentException("Некорректное количество");
    }
    private void remember(Long playerId, String id, String type, String result) {
        operationRepository.save(TradeOperation.builder().playerId(playerId).operationId(id).operationType(type)
                .result(result).createdAt(Instant.now()).build());
    }
}
