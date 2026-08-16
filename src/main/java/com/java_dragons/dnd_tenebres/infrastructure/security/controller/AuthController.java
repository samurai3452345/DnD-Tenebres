package com.java_dragons.dnd_tenebres.infrastructure.security.controller;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerService;
import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import com.java_dragons.dnd_tenebres.infrastructure.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PlayerService playerService;

    public record RegisterRequest(String username, String password, PlayerCreationRequest playerRequest) {}
    public record AuthResponse(String token) {}
    public record LoginRequest(String username, String password) {}

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (userAccountRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        PlayerResponse playerResponse = playerService.createPlayer(request.playerRequest());

        UserAccount account = UserAccount.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .playerId(playerResponse.getPlayerId())
                .build();
        userAccountRepository.save(account);

        UserDetails userDetails = userDetailsService.loadUserByUsername(account.getUsername());
        String token = jwtService.generateToken(account, userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserAccount account = userAccountRepository.findByUsername(request.username()).orElseThrow();
        UserDetails userDetails = userDetailsService.loadUserByUsername(account.getUsername());
        String token = jwtService.generateToken(account, userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}