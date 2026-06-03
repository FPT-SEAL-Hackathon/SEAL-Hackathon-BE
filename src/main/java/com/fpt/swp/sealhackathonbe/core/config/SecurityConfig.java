package com.fpt.swp.sealhackathonbe.core.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Khai báo danh sách các endpoint của Swagger cần được public
    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (Thường được tắt khi làm RESTful API phân quyền bằng Token)
                .csrf(AbstractHttpConfigurer::disable)

                // Cấu hình phân quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả mọi người truy cập vào các đường dẫn của Swagger
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // (Tùy chọn) Cho phép public thêm các API đăng nhập/đăng ký
                        // .requestMatchers("/api/auth/**").permitAll()

                        // Tất cả các request khác (API lấy dữ liệu...) đều bắt buộc phải có quyền (đã đăng nhập/có token)
                        .anyRequest().authenticated()
                );

        return http.build();
    }

}
