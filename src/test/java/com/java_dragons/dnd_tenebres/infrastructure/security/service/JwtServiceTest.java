package com.java_dragons.dnd_tenebres.infrastructure.security.service;

import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-that-is-longer-than-thirty-two-bytes";

    @Test
    void создаётИПроверяетТокенСПерсонажем() {
        JwtService jwtService = new JwtService(TEST_SECRET, Duration.ofHours(1));
        UserAccount account = UserAccount.create("игрок", "закодированный-пароль");
        UserDetails userDetails = new User("игрок", "закодированный-пароль", Collections.emptyList());

        String token = jwtService.generateToken(account, userDetails, 42L);

        assertThat(jwtService.extractUsername(token)).isEqualTo("игрок");
        assertThat(jwtService.extractPlayerId(token)).isEqualTo(42L);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void создаётТокенАккаунтаБезВыбранногоПерсонажа() {
        JwtService jwtService = new JwtService(TEST_SECRET, Duration.ofHours(1));
        UserAccount account = UserAccount.create("игрок", "закодированный-пароль");
        UserDetails userDetails = new User("игрок", "закодированный-пароль", Collections.emptyList());

        String token = jwtService.generateToken(account, userDetails);

        assertThat(jwtService.extractPlayerId(token)).isNull();
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void отклоняетСлишкомКороткийСекрет() {
        assertThatThrownBy(() -> new JwtService("короткий", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 байт");
    }
}
