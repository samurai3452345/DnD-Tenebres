package com.java_dragons.dnd_tenebres.infrastructure.web;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String requestId = req.getHeader("X-Request-ID");
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{8,100}")) requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId); res.setHeader("X-Request-ID", requestId);
        try { chain.doFilter(req, res); } finally { MDC.remove("requestId"); }
    }
}
