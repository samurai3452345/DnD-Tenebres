package com.java_dragons.dnd_tenebres.infrastructure.security.controller;

import com.java_dragons.dnd_tenebres.infrastructure.audit.AuditService;
import com.java_dragons.dnd_tenebres.infrastructure.metrics.GameMetrics;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.AuthResponse;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.LoginRequest;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.RegisterRequest;
import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import com.java_dragons.dnd_tenebres.infrastructure.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;
    private final GameMetrics gameMetrics;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);

        UserAccount account = findAccount(request.username());
        auditService.record(
                account.getUsername(),
                null,
                "REGISTER",
                "SUCCESS",
                "Account created"
        );
        gameMetrics.registration();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        UserAccount account = findAccount(request.username());
        auditService.record(
                account.getUsername(),
                null,
                "LOGIN",
                "SUCCESS",
                "Authentication successful"
        );

        return ResponseEntity.ok(response);
    }

    private UserAccount findAccount(String username) {
        return userAccountRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalStateException(
                        "Пользователь не найден после успешной авторизации"
                ));
    }
}
