package com.fpt.swp.sealhackathonbe.auth.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public boolean isActive() {
        return revokedAt == null
                && expiresAt != null
                && expiresAt.isAfter(LocalDateTime.now());
    }

    // token đã bị logout
    public boolean isRevoked() {
        return revokedAt != null;
    }
}
