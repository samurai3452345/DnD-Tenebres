package com.java_dragons.dnd_tenebres.domain.player.controller;

import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerCreationRequest;
import com.java_dragons.dnd_tenebres.domain.player.dto.PlayerResponse;
import com.java_dragons.dnd_tenebres.domain.player.service.PlayerService;
import com.java_dragons.dnd_tenebres.infrastructure.security.dto.AuthResponse;
import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import com.java_dragons.dnd_tenebres.infrastructure.security.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PlayerControllerTest {

    private PlayerService playerService;
    private UserAccountRepository accountRepository;
    private AuthService authService;
    private PlayerController controller;
    private UserAccount account;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        playerService = mock(PlayerService.class);
        accountRepository = mock(UserAccountRepository.class);
        authService = mock(AuthService.class);
        controller = new PlayerController(playerService, accountRepository, authService);
        account = UserAccount.builder()
                .id(7L)
                .username("account")
                .password("encoded")
                .build();
        authentication = new UsernamePasswordAuthenticationToken("account", null);
        when(accountRepository.findByUsername("account")).thenReturn(Optional.of(account));
    }

    @Test
    void создаётНесколькоПерсонажейДляОдногоАккаунта() {
        PlayerCreationRequest request = request("Первый");
        PlayerResponse first = PlayerResponse.builder().playerId(11L).build();
        PlayerResponse second = PlayerResponse.builder().playerId(12L).build();
        AuthResponse accountAuth = new AuthResponse("account-token", true, 2, null);

        when(playerService.createPlayer(7L, request)).thenReturn(first, second);
        when(authService.createResponse(account)).thenReturn(accountAuth);

        assertThat(controller.createPlayer(authentication, request)).isEqualTo(accountAuth);
        assertThat(controller.createPlayer(authentication, request)).isEqualTo(accountAuth);
        verify(playerService, times(2)).createPlayer(7L, request);
        verify(authService, times(2)).createResponse(account);
    }

    @Test
    void передВыдачейИгровогоТокенаПроверяетВладельцаПерсонажа() {
        AuthResponse response = new AuthResponse("selected-token", true, 2, 12L);
        when(authService.createResponse(account, 12L)).thenReturn(response);

        assertThat(controller.selectCharacter(authentication, 12L)).isEqualTo(response);

        var order = inOrder(playerService, authService);
        order.verify(playerService).verifyCharacterOwnership(7L, 12L);
        order.verify(authService).createResponse(account, 12L);
    }

    @Test
    void возвращаетСписокТолькоТекущегоАккаунта() {
        when(playerService.getCharacters(7L)).thenReturn(List.of());

        assertThat(controller.getCharacters(authentication)).isEmpty();
        verify(playerService).getCharacters(7L);
    }

    private PlayerCreationRequest request(String name) {
        return new PlayerCreationRequest(name, 15, 15, 15, 8, 8, 8);
    }
}
