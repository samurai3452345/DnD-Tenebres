package com.java_dragons.dnd_tenebres.domain.item.repository;

import com.java_dragons.dnd_tenebres.domain.item.entity.ForgeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ForgeOperationRepository extends JpaRepository<ForgeOperation, Long> {
    Optional<ForgeOperation> findByPlayerIdAndOperationId(Long playerId, String operationId);
}
