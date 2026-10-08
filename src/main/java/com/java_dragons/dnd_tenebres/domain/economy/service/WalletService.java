package com.java_dragons.dnd_tenebres.domain.economy.service;
import com.java_dragons.dnd_tenebres.domain.economy.entity.WalletTransaction;
import com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason;
import com.java_dragons.dnd_tenebres.domain.economy.repository.WalletTransactionRepository;
import com.java_dragons.dnd_tenebres.domain.player.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
@Service @RequiredArgsConstructor
public class WalletService {
    private final WalletTransactionRepository repository;
    public boolean canAfford(Player player, long amount) { return amount >= 0 && player.getGold() >= amount; }
    public long credit(Player player, long amount, WalletReason reason, String type, String id) {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        long before = player.getGold(); player.addGold(amount); save(player, amount, before, reason, type, id); return player.getGold();
    }
    public long debit(Player player, long amount, WalletReason reason, String type, String id) {
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");
        long before = player.getGold();
        if (!player.spendGold(amount)) throw new IllegalStateException("Недостаточно золота");
        save(player, -amount, before, reason, type, id); return player.getGold();
    }
    private void save(Player p, long amount, long before, WalletReason reason, String type, String ref) {
        repository.save(WalletTransaction.builder().playerId(p.getId()).playerName(p.getName())
                .playerReferenceId(p.getId())
                .amount(amount).balanceBefore(before)
                .balanceAfter(p.getGold()).reason(reason).referenceType(type).referenceId(ref).createdAt(Instant.now()).build());
    }
}
