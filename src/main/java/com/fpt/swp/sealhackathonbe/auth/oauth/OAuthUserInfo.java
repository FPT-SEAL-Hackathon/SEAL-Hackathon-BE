package com.fpt.swp.sealhackathonbe.auth.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Thông tin định danh lấy từ provider OAuth.
 * Định danh nghiệp vụ là (provider, providerUserId) — KHÔNG phải email.
 */
@Getter
@Builder
@AllArgsConstructor
public class OAuthUserInfo {

    private final String provider;
    private final String providerUserId;
    private final String email;
    private final Boolean emailVerified;
    private final String displayName;
    private final String avatarUrl;

    /**
     * Google trả claim chuẩn OIDC: sub / email / email_verified / name / picture.
     */
    public static OAuthUserInfo fromGoogle(Map<String, Object> attributes) {
        Object emailVerified = attributes.get("email_verified");
        return OAuthUserInfo.builder()
                .provider("GOOGLE")
                .providerUserId(stringValue(attributes.get("sub")))
                .email(stringValue(attributes.get("email")))
                .emailVerified(emailVerified instanceof Boolean bool
                        ? bool
                        : emailVerified != null && Boolean.parseBoolean(emailVerified.toString()))
                .displayName(stringValue(attributes.get("name")))
                .avatarUrl(stringValue(attributes.get("picture")))
                .build();
    }

    private static String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
