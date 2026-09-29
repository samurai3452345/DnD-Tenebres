package com.java_dragons.dnd_tenebres.domain.item.repository;
import com.java_dragons.dnd_tenebres.domain.item.entity.TradeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TradeOperationRepository extends JpaRepository<TradeOperation,Long> {
    Optional<TradeOperation> findByPlayerIdAndOperationId(Long playerId, String operationId);
}
