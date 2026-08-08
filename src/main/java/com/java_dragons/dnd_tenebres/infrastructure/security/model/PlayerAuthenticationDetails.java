package com.java_dragons.dnd_tenebres.infrastructure.security.model;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
public class PlayerAuthenticationDetails extends WebAuthenticationDetails {
    private final Long playerId;

    public PlayerAuthenticationDetails(HttpServletRequest request, Long playerId) {
        super(request);
        this.playerId = playerId;
    }
}