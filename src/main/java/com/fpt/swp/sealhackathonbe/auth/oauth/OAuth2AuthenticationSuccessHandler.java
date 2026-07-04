package com.fpt.swp.sealhackathonbe.auth.oauth;

import com.fpt.swp.sealhackathonbe.core.config.AppProperties;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Locale;

/**
 * Sau khi Google xác thực xong: tạo/tìm user theo (provider, providerUserId),
 * phát code trao đổi một lần rồi redirect về frontend.
 * Không đưa access/refresh token thô lên URL.
 */
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final OAuth2LoginService oauth2LoginService;
    private final OAuthCodeStore oauthCodeStore;
    private final AppProperties appProperties;

    public OAuth2AuthenticationSuccessHandler(
            OAuth2LoginService oauth2LoginService,
            OAuthCodeStore oauthCodeStore,
            AppProperties appProperties
    ) {
        this.oauth2LoginService = oauth2LoginService;
        this.oauthCodeStore = oauthCodeStore;
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String registrationId = authentication instanceof OAuth2AuthenticationToken token
                ? token.getAuthorizedClientRegistrationId()
                : "";

        if (!"google".equalsIgnoreCase(registrationId)) {
            // Hiện chỉ hỗ trợ Google trong luồng OAuth này.
            redirectWithError(request, response, "unsupported_provider");
            return;
        }

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        OAuthUserInfo info = OAuthUserInfo.fromGoogle(principal.getAttributes());

        User user;
        try {
            user = oauth2LoginService.loginOrCreate(info);
        } catch (Exception ex) {
            log.error("OAuth login failed for provider {}", registrationId.toUpperCase(Locale.ROOT), ex);
            redirectWithError(request, response, "oauth_login_failed");
            return;
        }

        String code = oauthCodeStore.issue(user.getUserId());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(appProperties.getFrontendUrl())
                .path("/oauth2/success")
                .queryParam("code", code)
                .build()
                .toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void redirectWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            String errorCode
    ) throws IOException {
        String redirectUrl = UriComponentsBuilder
                .fromUriString(appProperties.getFrontendUrl())
                .path("/oauth2/success")
                .queryParam("error", errorCode)
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
