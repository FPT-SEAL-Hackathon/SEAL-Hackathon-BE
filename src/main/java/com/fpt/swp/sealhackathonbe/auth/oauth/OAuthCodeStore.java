package com.fpt.swp.sealhackathonbe.auth.oauth;

import com.fpt.swp.sealhackathonbe.core.utils.TokenHashUtil;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kho code trao đổi OAuth một lần (in-memory, đủ cho triển khai một instance).
 * Backend redirect về frontend kèm code tạm thay vì token thô trên URL;
 * frontend gọi /api/v1/auth/oauth2/exchange để đổi code lấy phiên đăng nhập.
 */
@Component
public class OAuthCodeStore {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int CODE_BYTES = 32;
    private static final long CODE_TTL_SECONDS = 300;

    private final Map<String, Entry> codes = new ConcurrentHashMap<>();
    private final TokenHashUtil tokenHashUtil;

    public OAuthCodeStore(TokenHashUtil tokenHashUtil) {
        this.tokenHashUtil = tokenHashUtil;
    }

    /**
     * Phát code một lần cho user vừa hoàn tất OAuth; chỉ lưu hash của code.
     */
    public String issue(UUID userId) {
        cleanupExpired();
        byte[] bytes = new byte[CODE_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        codes.put(tokenHashUtil.hash(code), new Entry(userId, Instant.now().plusSeconds(CODE_TTL_SECONDS)));
        return code;
    }

    /**
     * Đổi code lấy userId; code chỉ dùng được đúng một lần và có hạn 5 phút.
     */
    public UUID consume(String code) {
        cleanupExpired();
        if (code == null || code.isBlank()) {
            return null;
        }
        Entry entry = codes.remove(tokenHashUtil.hash(code));
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return null;
        }
        return entry.userId();
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        codes.entrySet().removeIf(candidate -> candidate.getValue().expiresAt().isBefore(now));
    }

    private record Entry(UUID userId, Instant expiresAt) {
    }
}
