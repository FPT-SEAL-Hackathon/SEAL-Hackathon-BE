/**
 * JWT Authentication Filter
 * <p>
 * Chức năng:
 * - Chặn mọi request đi vào hệ thống
 * - Đọc JWT từ Authorization Header
 * - Xác thực tính hợp lệ của JWT
 * - Nạp thông tin người dùng từ database
 * - Đăng nhập người dùng vào SecurityContext của Spring Security
 * <p>
 * Luồng:
 * Request
 * ↓
 * JwtFilter
 * ↓
 * JWTService
 * ↓
 * UserDetailsService
 * ↓
 * SecurityContextHolder
 * ↓
 * Controller
 */
package com.fpt.swp.sealhackathonbe.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;


    @Override

    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getServletPath();

        if (path.equals("/auth/login")
                || path.equals("/auth/register")) {
            System.out.println("BYPASS JWT");
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader =
                request.getHeader("Authorization");

        String token = null;
        if (authHeader != null && !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Authorization header must start with 'Bearer '");
            return;
        }

        String username = null;

        // Authorization: Bearer eyJ...
        if (authHeader != null &&
                authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                if (token.isBlank()) {
                    writeUnauthorized(response, "Access token is missing");
                    return;
                }
                if (token.startsWith("Bearer ")) {
                    writeUnauthorized(response, "Do not include 'Bearer ' in Swagger Authorize; paste accessToken only");
                    return;
                }

                username = jwtService.extractUserName(token);
            } catch (ExpiredJwtException e) {
                writeUnauthorized(response, "Access token has expired; login again");
                return;
            } catch (JwtException | IllegalArgumentException e) {
                writeUnauthorized(response, "Access token is invalid");
                return;
            }
        }
        if (username != null &&
                SecurityContextHolder.getContext()
                        .getAuthentication() == null) {
            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(username);
            if (jwtService.validateToken(
                    token,
                    userDetails
            )) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
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
}
