package com.java_dragons.dnd_tenebres.infrastructure.security.repository;

import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}