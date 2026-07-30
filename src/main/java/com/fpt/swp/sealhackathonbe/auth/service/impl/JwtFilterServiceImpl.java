package com.fpt.swp.sealhackathonbe.auth.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.fpt.swp.sealhackathonbe.auth.service.mapper.JwtFilterService;
import com.fpt.swp.sealhackathonbe.core.exception.ErrorResponse;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private UserRepository userRepository;

    // Jackson 3 (tools.jackson) — cung phien ban ma Spring Boot 4 dung cho HTTP.
    // Giu static final thay vi @Autowired: filter duoc khoi tao rat som trong vong doi
    // servlet nen tranh phu thuoc bean; ObjectMapper thread-safe khi chi doc/ghi.
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Cache user da tra cuu theo userId, TTL ngan.
     *
     * VI SAO CAN: filter nay tra DB o MOI request (findByUserIdAndIsDeletedFalse) de
     * kiem tai khoan con Active/chua bi xoa. Do la ly do khoa/xoa tai khoan co hieu luc
     * ngay, nhung cung la 1 query moi request — gop phan lam can HikariCP pool.
     * Cache 60s giam xuong toi da 1 query/phut/user.
     *
     * DANH DOI da duoc chap nhan: khoa tai khoan co hieu luc cham toi TTL (60 giay).
     * Neu can tuc thi thi giam TTL hoac bo cache.
     *
     * Dung ConcurrentHashMap thay vi them thu vien cache: du an chua co Caffeine va
     * so user dong thoi nho; entry het han duoc don ngay khi doc (khong can job rieng).
     */
    private static final long USER_CACHE_TTL_MS = 60_000L;
    private static final int USER_CACHE_MAX_ENTRIES = 5_000;

    private record CachedUser(UserDetails details, long expiresAtMs) {
        boolean isFresh() {
            return System.currentTimeMillis() < expiresAtMs;
        }
    }

    private final Map<UUID, CachedUser> userCache = new ConcurrentHashMap<>();

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

        try {
            String username = jwtServiceImpl.extractUserName(token);
            String role = jwtServiceImpl.extractRole(token);
            String userIdClaim = jwtServiceImpl.extractUserId(token);

            // Chỉ ACCESS token mới có claim userId + role; refresh token không có.
            // Chặn tại đây để refresh token (hạn 7 ngày) không thể dùng làm
            // Bearer access token, đồng thời khỏi cần tra bảng RefreshTokens
            // trên mỗi request như trước.
            if (role == null || userIdClaim == null) {
                writeUnauthorized(request, response, "Token is invalid or expired");
                return;
            }

            if (username != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                // Email có thể trùng giữa tài khoản local và OAuth,
                // nên ưu tiên nạp đúng user theo claim userId trong JWT.
                UserDetails userDetails = resolveUserDetails(userIdClaim, username);

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
     * Nạp user theo claim userId (chính xác tuyệt đối); fallback theo email
     * cho các token cũ không có claim userId.
     */
    private UserDetails resolveUserDetails(String userIdClaim, String username) {
        if (userIdClaim != null && !userIdClaim.isBlank()) {
            try {
                UUID userId = UUID.fromString(userIdClaim);

                CachedUser cached = userCache.get(userId);
                if (cached != null) {
                    if (cached.isFresh()) {
                        return cached.details();
                    }
                    userCache.remove(userId); // het han -> don ngay khi doc
                }

                User user = userRepository
                        .findByUserIdAndIsDeletedFalse(userId)
                        .orElse(null);
                if (user != null) {
                    UserDetails details = new UserPrincipal(user);
                    // Chan cache phinh vo han neu co nhieu user (vd bot quet token).
                    if (userCache.size() >= USER_CACHE_MAX_ENTRIES) {
                        userCache.clear();
                    }
                    userCache.put(userId, new CachedUser(details, System.currentTimeMillis() + USER_CACHE_TTL_MS));
                    return details;
                }
                // Khong tim thay (bi xoa) -> bo cache cu de khong con dung ban da stale.
                userCache.remove(userId);
            } catch (IllegalArgumentException ignored) {
                // Claim userId không hợp lệ thì dùng email.
            }
        }
        return userDetailsService.loadUserByUsername(username);
    }

    /**
     * Trả lỗi 401 dạng JSON khi xác thực thất bại.
     *
     * Dung ObjectMapper + ErrorResponse thay vi noi chuoi tay: ban cu chi can message
     * chua mot dau " hoac ky tu xuong dong la body thanh JSON hong, frontend parse
     * that bai va chi hien "Request failed (401)". Cach nay cung dam bao hinh dang
     * response giong het GlobalExceptionHandler.
     */
    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String responseMessage = isEventRegistrationRequest(request)
                ? "Authentication is required to register for an event."
                : message;
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("UNAUTHORIZED")
                .message(responseMessage)
                .path(request.getRequestURI())
                .build();
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
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

        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            return tokenParam;
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
                || path.equals("/api/v1/auth/verify-email")
                || path.equals("/api/v1/auth/forgot-password")
                || path.equals("/api/v1/auth/reset-password")
                || path.startsWith("/api/v1/auth/oauth2/")
                || path.startsWith("/oauth2/authorization/")
                || path.startsWith("/login/oauth2/code/")) {
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
