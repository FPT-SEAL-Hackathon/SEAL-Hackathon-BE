package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.service.mapper.JwtFilterService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lọc JWT trên mỗi request để thiết lập người dùng và quyền trong SecurityContext.
 */
@Component
public class JwtFilterServiceImpl extends OncePerRequestFilter implements JwtFilterService {

    @Autowired
    private JwtServiceImpl jwtServiceImpl;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * JWT:
     * Xác thực Bearer token và gắn principal cho các bước phân quyền sau đó.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        if (isPublicRequest(request, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        RefreshToken tokenEntity =
                refreshTokenRepository.findByTokenHash(token).orElse(null);

        if (tokenEntity != null && tokenEntity.getRevokedAt() != null) {
            writeUnauthorized(request, response, "Token has been revoked");
            return;
        }

        try {
            String username = jwtServiceImpl.extractUserName(token);
            String role = jwtServiceImpl.extractRole(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (!userDetails.isEnabled()) {
                    writeUnauthorized(request, response, "User account is not active");
                    return;
                }

                if (jwtServiceImpl.validateToken(token, userDetails)) {
                    // RBAC:
                    // Role trong JWT được chuyển thành authority để @PreAuthorize kiểm tra.
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + (role != null ? role : "USER")
                            )
                    );

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        } catch (JwtException e) {
            writeUnauthorized(request, response, "Token is invalid or expired");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Trả lỗi 401 dạng JSON khi xác thực thất bại.
     */
    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String responseMessage = isEventRegistrationRequest(request)
                ? "Authentication is required to register for an event."
                : message;
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"status\":401,\"error\":\"UNAUTHORIZED\","
                        + "\"message\":\"" + responseMessage + "\","
                        + "\"timestamp\":\"" + LocalDateTime.now() + "\"}"
        );
    }

    /**
     * JWT:
     * Tách token thô khỏi header Authorization dạng Bearer.
     */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }

    private boolean isPublicRequest(HttpServletRequest request, String path) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (path.equals("/auth/login")
                || path.equals("/auth/register")
                || path.equals("/auth/refresh")
                || path.equals("/auth/resend-verification-email")
                || path.equals("/auth/verify-email")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh")
                || path.equals("/api/v1/auth/resend-verification-email")
                || path.equals("/api/v1/auth/verify-email")) {
            return true;
        }
        if (path.startsWith("/api/v1/public/")) {
            return true;
        }
        if (HttpMethod.GET.matches(request.getMethod())
                && (path.equals("/api/v1/events")
                || path.matches("/api/v1/events/[^/]+")
                || path.equals("/api/v1/awards/events/total-prize")
                || path.matches("/api/v1/awards/events/[^/]+/total-prize")
                || path.matches("/api/v1/awards/events/[^/]+")
                || path.matches("/api/v1/categories/categories/[^/]+"))) {
            return true;
        }
        return false;
    }

    private boolean isEventRegistrationRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        return HttpMethod.POST.matches(request.getMethod())
                && (path.matches("/api/v1/events/[^/]+/participants/register")
                || path.matches("/api/v1/events/[^/]+/register"));
    }
}
