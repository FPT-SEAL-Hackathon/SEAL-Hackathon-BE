package com.fpt.swp.sealhackathonbe.core.config;

import com.fpt.swp.sealhackathonbe.auth.service.impl.JwtFilterServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

/**
 * Cấu hình bảo mật stateless bằng JWT và bật kiểm tra quyền theo method.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilterServiceImpl jwtFilterServiceImpl;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * RBAC:
     * Các endpoint công khai không cần JWT để bootstrap xác thực.
     */
    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/error",
            "/",
            "/auth/login",
            "/auth/register",
            "/auth/resend-verification-email",
            "/auth/refresh",
            "/auth/verify-email",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/resend-verification-email",
            "/api/v1/auth/refresh",
            "/api/v1/auth/verify-email",
            "/api/v1/public/**",
            "/api/v1/public/**",
            "/api/v1/awards/events/total-prize"  // Public: landing page stats
    };

    /**
     * RBAC:
     * Mọi endpoint ngoài whitelist phải có JWT hợp lệ trước khi vào controller.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        .requestMatchers(SWAGGER_WHITELIST)
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/events", "/api/v1/events/*")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/awards/events/total-prize", "/api/v1/awards/events/*/total-prize")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/awards/events/*", "/api/v1/categories/categories/*")
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            String message = isEventRegistrationRequest(request.getMethod(), request.getServletPath())
                                    ? "Authentication is required to register for an event."
                                    : "Authorization header is missing or token was not accepted";
                            response.getWriter().write(
                                    "{\"success\":false,\"status\":401,\"error\":\"UNAUTHORIZED\","
                                            + "\"message\":\"" + message + "\","
                                            + "\"timestamp\":\"" + LocalDateTime.now() + "\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            String message = accessDeniedException.getMessage() != null
                                    && !accessDeniedException.getMessage().isBlank()
                                    && !"Access Denied".equals(accessDeniedException.getMessage())
                                    ? accessDeniedException.getMessage()
                                    : "You don't have permission to do this.";
                            response.getWriter().write(
                                    "{\"success\":false,\"status\":403,\"error\":\"ACCESS_DENIED\","
                                            + "\"message\":\"" + message + "\","
                                            + "\"timestamp\":\"" + LocalDateTime.now() + "\"}"
                            );
                        })
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .addFilterBefore(
                        jwtFilterServiceImpl,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Password:
     * Dùng BCrypt để kiểm tra mật khẩu đã mã hóa trong quá trình đăng nhập.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(
                new BCryptPasswordEncoder(12)
        );

        return provider;
    }

    /**
     * Cung cấp AuthenticationManager cho luồng đăng nhập.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private boolean isEventRegistrationRequest(String method, String path) {
        return HttpMethod.POST.matches(method)
                && (path.matches("/api/v1/events/[^/]+/participants/register")
                || path.matches("/api/v1/events/[^/]+/register"));
    }
}
