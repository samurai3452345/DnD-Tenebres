package com.java_dragons.dnd_tenebres.domain.item.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity @Table(name="trade_operations", uniqueConstraints=@UniqueConstraint(name="uq_trade_operation", columnNames={"player_id","operation_id"}))
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class TradeOperation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="player_id", nullable=false) private Long playerId;
    @Column(name="operation_id", nullable=false) private String operationId;
    @Column(name="operation_type", nullable=false) private String operationType;
    @Column(name="request_hash", nullable=false, length=64) private String requestHash;
    @Column(name="resource_id", nullable=false) private Long resourceId;
    @Column(name="amount", nullable=false) private int amount;
    @Column(nullable=false, length=1000) private String result;
    @Column(name="created_at", nullable=false) private Instant createdAt;
}
