package com.java_dragons.dnd_tenebres.infrastructure.security.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-10-08T10:00:00Z"), ZoneOffset.UTC);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void normalizesArbitraryPathsAndBoundsStoredWindows() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(clock, false, 5, 10, 1000, 1000, 1000);

        for (int i = 0; i < 50; i++) {
            perform(filter, "/api/v1/random-" + i + "/anything", "198.51.100." + i);
        }

        assertThat(filter.trackedWindowCount()).isLessThanOrEqualTo(5);
        assertThat(RateLimitFilter.normalizedRoute("/api/v1/players/123/items/999"))
                .isEqualTo("/api/v1/players");
    }

    @Test
    void limitsAuthenticatedAccountIndependentlyFromIp() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(clock, false, 50, 10, 100, 2, 100);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("hero", null, java.util.List.of())
        );

        assertThat(perform(filter, "/api/v1/players/current", "198.51.100.1").getStatus()).isEqualTo(200);
        assertThat(perform(filter, "/api/v1/players/current", "198.51.100.2").getStatus()).isEqualTo(200);
        assertThat(perform(filter, "/api/v1/players/current", "198.51.100.3").getStatus()).isEqualTo(429);
    }

    @Test
    void ignoresForwardedAddressUnlessProxyIsTrusted() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(clock, false, 50, 1, 100, 100, 100);
        MockHttpServletRequest first = request("/api/v1/auth/login", "198.51.100.1");
        first.addHeader("X-Forwarded-For", "203.0.113.1");
        MockHttpServletRequest second = request("/api/v1/auth/login", "198.51.100.1");
        second.addHeader("X-Forwarded-For", "203.0.113.2");

        assertThat(perform(filter, first).getStatus()).isEqualTo(200);
        assertThat(perform(filter, second).getStatus()).isEqualTo(429);
    }

    private MockHttpServletResponse perform(RateLimitFilter filter, String path, String remoteAddress)
            throws Exception {
        return perform(filter, request(path, remoteAddress));
    }

    private MockHttpServletRequest request(String path, String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setRemoteAddr(remoteAddress);
        return request;
    }

    private MockHttpServletResponse perform(RateLimitFilter filter, MockHttpServletRequest request)
            throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
