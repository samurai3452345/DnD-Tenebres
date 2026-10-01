package com.java_dragons.dnd_tenebres.infrastructure.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component @Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register") ||
                ("POST".equals(request.getMethod()) && path.startsWith("/api/v1/")));
    }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        boolean auth = req.getRequestURI().startsWith("/api/v1/auth/");
        int limit = auth ? 10 : 120;
        long minute = Instant.now().getEpochSecond() / 60;
        String key = req.getRemoteAddr() + ':' + req.getRequestURI();
        Window w = windows.compute(key, (k, old) -> old == null || old.minute != minute ? new Window(minute) : old);
        if (w.count.incrementAndGet() > limit) {
            res.setStatus(429); res.setContentType("application/json");
            res.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many requests\"}");
            return;
        }
        chain.doFilter(req, res);
    }
    private static final class Window { final long minute; final AtomicInteger count = new AtomicInteger(); Window(long m){minute=m;} }
}
