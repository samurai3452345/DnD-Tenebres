package com.java_dragons.dnd_tenebres.infrastructure.security.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "player_id")
    private Long playerId;

    public static UserAccount create(String username, String encodedPassword, Long playerId) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        UserAccount account = new UserAccount();
        account.username = username;
        account.changePassword(encodedPassword);
        account.linkPlayer(playerId);
        return account;
    }

    public void changePassword(String encodedNewPassword) {
        if (encodedNewPassword == null || encodedNewPassword.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        this.password = encodedNewPassword;
    }

    public void linkPlayer(Long newPlayerId) {
        if (newPlayerId == null) {
            throw new IllegalArgumentException("ID персонажа не указан");
        }
        if (this.playerId != null) {
            throw new IllegalStateException("К этому аккаунту уже привязан персонаж!");
        }
        this.playerId = newPlayerId;
    }
}
