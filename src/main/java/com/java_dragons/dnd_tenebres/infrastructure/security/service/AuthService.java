package com.java_dragons.dnd_tenebres.infrastructure.security.service;

import com.java_dragons.dnd_tenebres.infrastructure.security.dto.AuthResponse;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.LoginRequest;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.RegisterRequest;
import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        if (userAccountRepository.existsByUsername(username)) {
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.password())
        );

        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return createResponse(account);
    }

    public AuthResponse createResponse(UserAccount account) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(account.getUsername());
        return new AuthResponse(jwtService.generateToken(account, userDetails), account.getPlayerId() != null);
    }
}
