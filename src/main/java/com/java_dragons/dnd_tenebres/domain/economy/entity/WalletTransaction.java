package com.java_dragons.dnd_tenebres.domain.economy.entity;
import com.java_dragons.dnd_tenebres.domain.economy.model.WalletReason;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name = "wallet_transactions", indexes = @Index(name="idx_wallet_player", columnList="player_id,id"))
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class WalletTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="player_id", nullable=false) private Long playerId;
    @Column(nullable=false) private long amount;
    @Column(name="balance_before", nullable=false) private long balanceBefore;
    @Column(name="balance_after", nullable=false) private long balanceAfter;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private WalletReason reason;
    @Column(name="reference_type") private String referenceType;
    @Column(name="reference_id") private String referenceId;
    @Column(name="created_at", nullable=false) private Instant createdAt;
}
