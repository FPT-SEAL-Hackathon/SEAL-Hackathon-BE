package com.fpt.swp.sealhackathonbe.core.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limit đơn giản (in-memory, sliding window) cho các endpoint auth
 * nhạy cảm: chống brute-force mật khẩu và spam email verify/reset.
 * Giới hạn theo IP + path; đủ dùng cho triển khai một instance.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_WINDOW = 10;
    private static final long WINDOW_MILLIS = 60_000L;

    // Chỉ POST vào các path này mới bị giới hạn.
    private static final Set<String> LIMITED_PATHS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/resend-verification-email",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/resend-verification-email",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    );

    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !LIMITED_PATHS.contains(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String key = clientIp(request) + "|" + request.getServletPath();
        long now = System.currentTimeMillis();

        Deque<Long> timestamps = requestLog.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        boolean allowed;
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }
            allowed = timestamps.size() < MAX_REQUESTS_PER_WINDOW;
            if (allowed) {
                timestamps.addLast(now);
            }
        }

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"status\":429,\"error\":\"TOO_MANY_REQUESTS\","
                            + "\"message\":\"Too many attempts. Please try again in a minute.\","
                            + "\"timestamp\":\"" + LocalDateTime.now() + "\"}"
            );
            return;
        }

        // Dọn rác cơ hội: tránh map phình vô hạn theo số IP đã từng gọi.
        if (requestLog.size() > 10_000) {
            requestLog.entrySet().removeIf(entry -> {
                synchronized (entry.getValue()) {
                    return entry.getValue().isEmpty()
                            || now - entry.getValue().peekLast() > WINDOW_MILLIS;
                }
            });
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
