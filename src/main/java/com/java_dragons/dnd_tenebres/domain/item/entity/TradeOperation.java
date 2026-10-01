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
    @Column(nullable=false, length=1000) private String result;
    @Column(name="created_at", nullable=false) private Instant createdAt;
}
