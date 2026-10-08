package com.java_dragons.dnd_tenebres.infrastructure.security.service;

import com.java_dragons.dnd_tenebres.infrastructure.security.dto.AuthResponse;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.LoginRequest;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.RegisterRequest;
import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PlayerRepository playerRepository;
    private final com.java_dragons.dnd_tenebres.infrastructure.audit.AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userAccountRepository.existsByUsername(username)) {
            auditService.record(username, null, "REGISTER", "DENIED", "Username already exists");
            throw new IllegalStateException("Пользователь с таким именем уже существует");
        }

        UserAccount account = UserAccount.create(
                username,
                passwordEncoder.encode(request.password())
        );
        userAccountRepository.saveAndFlush(account);

        return createResponse(account);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        } catch (AuthenticationException exception) {
            auditService.record(username, null, "LOGIN", "DENIED", "Invalid credentials");
            throw exception;
        }

        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        auditService.record(username, null, "LOGIN", "SUCCESS", "Authenticated");
        return createResponse(account);
    }

    public AuthResponse createResponse(UserAccount account) {
        return createResponse(account, null);
    }

    public AuthResponse createResponse(UserAccount account, Long selectedPlayerId) {
        long characterCount = playerRepository.countByAccountId(account.getId());
        return new AuthResponse(
                jwtService.generateToken(account, selectedPlayerId),
                characterCount > 0,
                characterCount,
                selectedPlayerId
        );
    }
}
