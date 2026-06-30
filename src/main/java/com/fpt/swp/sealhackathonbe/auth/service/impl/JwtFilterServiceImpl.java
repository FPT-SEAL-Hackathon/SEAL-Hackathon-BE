package com.fpt.swp.sealhackathonbe.auth.service.impl;

import com.fpt.swp.sealhackathonbe.auth.entity.RefreshToken;
import com.fpt.swp.sealhackathonbe.auth.repository.RefreshTokenRepository;
import com.fpt.swp.sealhackathonbe.auth.service.mapper.JwtFilterService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
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

        if (path.equals("/auth/login") || path.equals("/auth/register") || path.equals("/auth/refresh")) {
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String username = jwtServiceImpl.extractUserName(token);
            String role = jwtServiceImpl.extractRole(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Trả lỗi 401 dạng JSON khi xác thực thất bại.
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}"
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
}
