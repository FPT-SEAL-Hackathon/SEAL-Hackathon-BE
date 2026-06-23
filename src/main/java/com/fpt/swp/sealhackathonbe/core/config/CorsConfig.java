package com.fpt.swp.sealhackathonbe.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cho tất cả các endpoint API
                .allowedOrigins(
                    "http://localhost:5173", // Cho phép Vite (React Frontend) gọi tới
                    "http://localhost:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Các HTTP method được phép
                .allowedHeaders("*") // Chấp nhận mọi Header (như Authorization, Content-Type...)
                .allowCredentials(true) // Cho phép gửi Cookies hoặc thông tin Authentication
                .maxAge(3600); // Cache cấu hình CORS trong 1 giờ (3600 giây) để giảm tải request OPTIONS (Preflight)
    }
}
