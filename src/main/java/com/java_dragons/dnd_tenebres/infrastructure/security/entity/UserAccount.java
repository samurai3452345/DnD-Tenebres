package com.java_dragons.dnd_tenebres.infrastructure.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_accounts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public static UserAccount create(
            String username,
            String encodedPassword
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Имя пользователя не может быть пустым"
            );
        }

        UserAccount account = new UserAccount();
        account.username = username.trim();
        account.changePassword(encodedPassword);
        return account;
    }

    public void changePassword(String encodedNewPassword) {
        if (encodedNewPassword == null || encodedNewPassword.isBlank()) {
            throw new IllegalArgumentException(
                    "Пароль не может быть пустым"
            );
        }

        this.password = encodedNewPassword;
    }

    public void revokeAllTokens() {
        this.tokenVersion++;
    }

    public void disable() {
        this.enabled = false;
        this.tokenVersion++;
    }
}
