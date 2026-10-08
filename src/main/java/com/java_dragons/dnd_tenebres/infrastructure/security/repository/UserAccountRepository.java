package com.java_dragons.dnd_tenebres.infrastructure.security.repository;

import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserAccount account
               set account.tokenVersion = account.tokenVersion + 1,
                   account.version = account.version + 1
             where account.username = :username and account.enabled = true
            """)
    int incrementTokenVersion(@Param("username") String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update UserAccount account
               set account.enabled = false,
                   account.tokenVersion = account.tokenVersion + 1,
                   account.version = account.version + 1
             where account.username = :username and account.enabled = true
            """)
    int disableAccount(@Param("username") String username);
}
