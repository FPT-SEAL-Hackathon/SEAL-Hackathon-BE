package com.fpt.swp.sealhackathonbe.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String frontEndUrl = System.getenv("FRONT_END_URL");
        List<String> origins = (frontEndUrl != null && !frontEndUrl.trim().isEmpty())
                ? Arrays.asList("http://localhost:5173", "http://localhost:3000", frontEndUrl)
                : Arrays.asList("http://localhost:5173", "http://localhost:3000");

        CorsConfiguration configuration = new CorsConfiguration();
        // Fallback to allow any origin pattern if exact origin is tricky to configure on Azure
        if (origins.size() == 2) { 
            // If FRONT_END_URL wasn't set, allow all to prevent production crash
            configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        } else {
            configuration.setAllowedOrigins(origins);
            configuration.setAllowedOriginPatterns(Arrays.asList(frontEndUrl, "https://*.azurestaticapps.net"));
        }
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
