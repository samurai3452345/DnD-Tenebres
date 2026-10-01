package com.java_dragons.dnd_tenebres.domain.economy.dto;
import java.time.Instant;
public record WalletTransactionResponse(Long id, long amount, long balanceBefore, long balanceAfter,
 String reason, String referenceType, String referenceId, Instant createdAt) {}
