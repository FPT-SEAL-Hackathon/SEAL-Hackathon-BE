package com.fpt.swp.sealhackathonbe.auth.service.impl;

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

@Component
public class JwtFilterServiceImpl extends OncePerRequestFilter {

    @Autowired
    private JwtServiceImpl jwtServiceImpl;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        if (path.equals("/auth/login") || path.equals("/auth/register") ||
                path.equals("/auth/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // 🔥 ONLY 1 TIME PARSE
            String username = jwtServiceImpl.extractUserName(token);
            String role = jwtServiceImpl.extractRole(token);

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                if (jwtServiceImpl.validateToken(token, userDetails)) {

                    // 🔥 SAFE ROLE HANDLING
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

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}"
        );
    }
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}
