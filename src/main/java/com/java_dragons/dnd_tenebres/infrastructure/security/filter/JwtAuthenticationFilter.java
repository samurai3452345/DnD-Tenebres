package com.java_dragons.dnd_tenebres.infrastructure.security.filter;

import com.java_dragons.dnd_tenebres.infrastructure.security.model.PlayerAuthenticationDetails;
import com.java_dragons.dnd_tenebres.infrastructure.security.entity.UserAccount;
import com.java_dragons.dnd_tenebres.infrastructure.security.repository.UserAccountRepository;
import com.java_dragons.dnd_tenebres.infrastructure.security.service.JwtService;
import com.java_dragons.dnd_tenebres.domain.player.repository.PlayerRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserAccountRepository accountRepository;
    private final PlayerRepository playerRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            JwtService.JwtClaims claims = jwtService.parseAndValidate(jwt);
            String username = claims.username();

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserAccount account =
                        accountRepository.findByUsername(username).orElse(null);

                boolean accountIsValid =
                        account != null
                                && account.isEnabled()
                                && claims.tokenVersion() == account.getTokenVersion();

                if (accountIsValid) {
                    Long playerId = claims.playerId();
                    if (playerId != null && !playerRepository.existsByIdAndAccountId(playerId, account.getId())) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    UserDetails userDetails = User.withUsername(account.getUsername())
                            .password(account.getPassword()).authorities(java.util.List.of()).build();
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new PlayerAuthenticationDetails(request, playerId)
                    );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            }
        } catch (JwtException
                 | IllegalArgumentException
                 | AuthenticationException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
