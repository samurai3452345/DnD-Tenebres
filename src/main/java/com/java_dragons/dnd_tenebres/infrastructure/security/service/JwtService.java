package com.java_dragons.dnd_tenebres.infrastructure.security.service;

import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final SecretKey previousSigningKey;
    private final long expirationMillis;
    private final String issuer;
    private final String audience;

    public JwtService(@Value("${application.security.jwt.secret}") String secret,
                      @Value("${application.security.jwt.expiration:PT24H}") Duration expiration,
                      @Value("${application.security.jwt.issuer:dnd-tenebres}") String issuer,
                      @Value("${application.security.jwt.audience:dnd-tenebres-api}") String audience,
                      @Value("${application.security.jwt.previous-secret:}") String previousSecret) {
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET должен содержать не менее 32 байт");
        }
        if (expiration == null || expiration.isZero() || expiration.isNegative()) {
            throw new IllegalStateException("Срок действия JWT должен быть положительным");
        }
        if (issuer.isBlank() || audience.isBlank()) throw new IllegalStateException("JWT issuer/audience обязательны");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        if (previousSecret != null && !previousSecret.isBlank()
                && previousSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_PREVIOUS_SECRET должен содержать не менее 32 байт");
        }
        this.previousSigningKey = previousSecret == null || previousSecret.isBlank() ? null
                : Keys.hmacShaKeyFor(previousSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expiration.toMillis();
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(UserAccount account, Long playerId) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(account.getUsername())
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .claim("tokenVersion", account.getTokenVersion());
        if (playerId != null) builder.claim("playerId", playerId);
        return builder.signWith(signingKey, Jwts.SIG.HS256).compact();
    }

    /** Signature, issuer, audience and expiration are checked exactly once per request. */
    public JwtClaims parseAndValidate(String token) {
        Claims claims;
        try {
            claims = parse(token, signingKey);
        } catch (JwtException currentKeyFailure) {
            if (previousSigningKey == null) throw currentKeyFailure;
            claims = parse(token, previousSigningKey);
        }
        Number playerId = number(claims, "playerId");
        Number tokenVersion = number(claims, "tokenVersion");
        return new JwtClaims(claims.getSubject(), playerId == null ? null : playerId.longValue(),
                tokenVersion == null ? 0 : tokenVersion.intValue());
    }

    private Claims parse(String token, SecretKey key) {
        return Jwts.parser().verifyWith(key).requireIssuer(issuer).requireAudience(audience)
                .build().parseSignedClaims(token).getPayload();
    }

    private Number number(Claims claims, String name) {
        Object value = claims.get(name);
        return value instanceof Number number ? number : null;
    }

    public record JwtClaims(String username, Long playerId, int tokenVersion) {}
}
