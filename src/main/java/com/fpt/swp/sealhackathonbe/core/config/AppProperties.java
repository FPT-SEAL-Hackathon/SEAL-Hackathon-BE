package com.fpt.swp.sealhackathonbe.core.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties implements InitializingBean {
    @NotBlank(message = "app.frontend-url must be configured with FRONTEND_URL")
    private String frontendUrl;

    @NotBlank(message = "app.backend-url must be configured with BACKEND_URL")
    private String backendUrl;

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = normalize(frontendUrl);
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = normalize(backendUrl);
    }

    /**
     * Dừng ứng dụng sớm khi URL triển khai bắt buộc bị thiếu hoặc không hợp lệ.
     */
    @Override
    public void afterPropertiesSet() {
        validateAbsoluteUrl("app.frontend-url", frontendUrl, "FRONTEND_URL");
        validateAbsoluteUrl("app.backend-url", backendUrl, "BACKEND_URL");
    }

    /**
     * Chuẩn hóa base URL trước khi nối thêm path.
     */
    private String normalize(String url) {
        if (url == null) {
            return null;
        }

        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * Đảm bảo URL dùng cho email và CORS đến từ host hợp lệ theo từng môi trường.
     */
    private void validateAbsoluteUrl(String propertyName, String value, String environmentVariable) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    propertyName + " must be configured with environment variable " + environmentVariable
            );
        }

        URI uri;
        try {
            uri = URI.create(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    propertyName + " must be a valid absolute URL configured by " + environmentVariable,
                    exception
            );
        }

        if (uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalStateException(
                    propertyName + " must be an absolute URL configured by " + environmentVariable
            );
        }

        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalStateException(
                    propertyName + " must use http or https and be configured by " + environmentVariable
            );
        }
    }
}
