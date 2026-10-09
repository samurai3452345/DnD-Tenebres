package com.java_dragons.dnd_tenebres.infrastructure.security.service;

import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-that-is-longer-than-thirty-two-bytes";

    @Test
    void создаётИПроверяетТокенСПерсонажем() {
        JwtService jwtService = service();
        UserAccount account = UserAccount.create("игрок", "закодированный-пароль");
        String token = jwtService.generateToken(account, 42L);

        assertThat(jwtService.parseAndValidate(token))
                .extracting(JwtService.JwtClaims::username, JwtService.JwtClaims::playerId)
                .containsExactly("игрок", 42L);
    }

    @Test
    void создаётТокенАккаунтаБезВыбранногоПерсонажа() {
        JwtService jwtService = service();
        UserAccount account = UserAccount.create("игрок", "закодированный-пароль");
        String token = jwtService.generateToken(account, null);

        assertThat(jwtService.parseAndValidate(token).playerId()).isNull();
    }

    @Test
    void отклоняетСлишкомКороткийСекрет() {
        assertThatThrownBy(() -> new JwtService("короткий", Duration.ofHours(1), "issuer", "audience", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 байт");
    }

    @Test
    void отклоняетТокенДругогоAudience() {
        JwtService issuer = service();
        JwtService otherAudience = new JwtService(TEST_SECRET, Duration.ofHours(1), "dnd-tenebres", "other-api", "");
        String token = issuer.generateToken(UserAccount.create("игрок", "hash"), null);
        assertThatThrownBy(() -> otherAudience.parseAndValidate(token)).isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    private JwtService service() {
        return new JwtService(TEST_SECRET, Duration.ofHours(1), "dnd-tenebres", "dnd-tenebres-api", "");
    }
}
