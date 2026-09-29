package com.java_dragons.dnd_tenebres.infrastructure.security.controller;

import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import com.java_dragons.dnd_tenebres.infrastructure.security.annotation.CurrentPlayerId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/account") @RequiredArgsConstructor
public class AccountController {
    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final com.java_dragons.dnd_tenebres.infrastructure.audit.AuditService auditService;
    public record AccountResponse(String username, Long playerId, int tokenVersion, boolean enabled) {}
    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min=8,max=100) String newPassword) {}

    @GetMapping
    public ResponseEntity<AccountResponse> account(Authentication auth) {
        var a = repository.findByUsername(auth.getName()).orElseThrow();
        return ResponseEntity.ok(new AccountResponse(a.getUsername(), a.getPlayerId(), a.getTokenVersion(), a.isEnabled()));
    }

    @PostMapping("/change-password") @Transactional
    public ResponseEntity<Void> changePassword(Authentication auth, @RequestBody @Valid ChangePasswordRequest request) {
        var a = repository.findByUsername(auth.getName()).orElseThrow();
        if (!passwordEncoder.matches(request.currentPassword(), a.getPassword()))
            throw new IllegalArgumentException("Неверный текущий пароль");
        a.changePassword(passwordEncoder.encode(request.newPassword()));
        a.revokeAllTokens();
        auditService.record(a.getUsername(), a.getPlayerId(), "PASSWORD_CHANGE", "SUCCESS", "All tokens revoked");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all") @Transactional
    public ResponseEntity<Void> logoutAll(Authentication auth) {
        repository.findByUsername(auth.getName()).orElseThrow().revokeAllTokens();
        var a = repository.findByUsername(auth.getName()).orElseThrow();
        auditService.record(a.getUsername(), a.getPlayerId(), "LOGOUT_ALL", "SUCCESS", "All tokens revoked");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping @Transactional
    public ResponseEntity<Void> delete(Authentication auth) {
        var a = repository.findByUsername(auth.getName()).orElseThrow();
        a.disable();
        auditService.record(a.getUsername(), a.getPlayerId(), "ACCOUNT_DELETE", "SUCCESS", "Account disabled");
        return ResponseEntity.noContent().build();
    }
}
