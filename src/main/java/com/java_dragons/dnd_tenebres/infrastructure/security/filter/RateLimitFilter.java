package com.java_dragons.dnd_tenebres.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_SECONDS = 60;
    private static final long STALE_AFTER_WINDOWS = 2;

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();
    private final Clock clock;
    private final boolean trustedProxy;
    private final int maxEntries;
    private final int authLimit;
    private final int anonymousLimit;
    private final int accountLimit;
    private final int authenticatedIpLimit;

    @Autowired
    public RateLimitFilter(
            @Value("${application.security.rate-limit.trusted-proxy:false}") boolean trustedProxy,
            @Value("${application.security.rate-limit.max-entries:10000}") int maxEntries,
            @Value("${application.security.rate-limit.auth-requests-per-minute:10}") int authLimit,
            @Value("${application.security.rate-limit.anonymous-requests-per-minute:120}") int anonymousLimit,
            @Value("${application.security.rate-limit.account-requests-per-minute:120}") int accountLimit,
            @Value("${application.security.rate-limit.authenticated-ip-requests-per-minute:600}") int authenticatedIpLimit
    ) {
        this(Clock.systemUTC(), trustedProxy, maxEntries, authLimit, anonymousLimit, accountLimit, authenticatedIpLimit);
    }

    RateLimitFilter(Clock clock, boolean trustedProxy, int maxEntries, int authLimit,
                    int anonymousLimit, int accountLimit, int authenticatedIpLimit) {
        this.clock = clock;
        this.trustedProxy = trustedProxy;
        this.maxEntries = requirePositive(maxEntries, "maxEntries");
        this.authLimit = requirePositive(authLimit, "authLimit");
        this.anonymousLimit = requirePositive(anonymousLimit, "anonymousLimit");
        this.accountLimit = requirePositive(accountLimit, "accountLimit");
        this.authenticatedIpLimit = requirePositive(authenticatedIpLimit, "authenticatedIpLimit");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith("/api/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long windowNumber = clock.instant().getEpochSecond() / WINDOW_SECONDS;
        cleanupIfNeeded(windowNumber);

        String route = normalizedRoute(request.getRequestURI());
        String clientIp = clientIp(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());

        boolean allowed;
        if (route.startsWith("/api/v1/auth/")) {
            allowed = allow("auth:ip:" + clientIp + ':' + route, authLimit, windowNumber);
        } else if (authenticated) {
            boolean ipAllowed = allow("api:ip:" + clientIp + ':' + route, authenticatedIpLimit, windowNumber);
            boolean accountAllowed = allow("api:account:" + authentication.getName() + ':' + route,
                    accountLimit, windowNumber);
            allowed = ipAllowed && accountAllowed;
        } else {
            allowed = allow("api:anonymous:" + clientIp + ':' + route, anonymousLimit, windowNumber);
        }
        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setHeader("Retry-After", Long.toString(WINDOW_SECONDS));
            response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Слишком много запросов\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean allow(String key, int limit, long windowNumber) {
        Window window = windows.get(key);
        if (window == null || window.number != windowNumber) {
            synchronized (windows) {
                window = windows.get(key);
                if (window == null && windows.size() >= maxEntries) {
                    return false;
                }
                if (window == null || window.number != windowNumber) {
                    window = new Window(windowNumber);
                    windows.put(key, window);
                }
            }
        }
        return window.count.incrementAndGet() <= limit;
    }

    private void cleanupIfNeeded(long currentWindow) {
        long count = requestCounter.incrementAndGet();
        if ((count & 255) != 0 && windows.size() < maxEntries) {
            return;
        }

        windows.entrySet().removeIf(entry -> entry.getValue().number < currentWindow - STALE_AFTER_WINDOWS);
    }

    private String clientIp(HttpServletRequest request) {
        if (trustedProxy) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",", 2)[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    static String normalizedRoute(String requestUri) {
        String[] segments = requestUri.split("/");
        if (segments.length < 4) {
            return "/api/v1";
        }
        String resource = segments[3].replaceAll("[^A-Za-z0-9_-]", "_");
        if ("auth".equals(resource) && segments.length >= 5) {
            String action = segments[4].replaceAll("[^A-Za-z0-9_-]", "_");
            return "/api/v1/auth/" + action;
        }
        return "/api/v1/" + resource;
    }

    int trackedWindowCount() {
        return windows.size();
    }

    private static int requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static final class Window {
        private final long number;
        private final AtomicInteger count = new AtomicInteger();

        private Window(long number) {
            this.number = number;
        }
    }
}
