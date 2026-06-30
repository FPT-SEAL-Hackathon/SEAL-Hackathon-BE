package com.fpt.swp.sealhackathonbe.auth.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lưu refresh token để quản lý phiên, refresh và thu hồi khi logout.
 */
@Getter
@Setter
@Entity
@Table(name = "RefreshTokens")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(name = "TokenID")
    @GeneratedValue
    private UUID tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "TokenHash", nullable = false, unique = true, length = 512)
    private String tokenHash;

    @Column(name = "IssuedAt", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "RevokedAt")
    private LocalDateTime revokedAt;

    @Column(name = "DeviceInfo", length = 500)
    private String deviceInfo;

    /**
     * Token chỉ còn dùng được khi chưa bị thu hồi và chưa hết hạn.
     */
    public boolean isActive() {
        return revokedAt == null
                && expiresAt != null
                && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Cho biết token đã bị logout hoặc bị thu hồi thủ công.
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }
}
