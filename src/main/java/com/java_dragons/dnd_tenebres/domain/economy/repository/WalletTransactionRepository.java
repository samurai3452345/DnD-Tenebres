package com.java_dragons.dnd_tenebres.domain.economy.repository;
import com.java_dragons.dnd_tenebres.domain.economy.entity.WalletTransaction;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByPlayerIdOrderByIdDesc(Long playerId, Pageable pageable);
}
