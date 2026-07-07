package com.fpt.swp.sealhackathonbe.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Định danh OAuth của một user.
 * Định danh duy nhất là (Provider, ProviderUserID) — KHÔNG dùng email,
 * vì email Google có thể trùng với tài khoản local mà không được auto-merge.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "UserOAuthAccounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"Provider", "ProviderUserID"}),
                @UniqueConstraint(columnNames = {"UserID", "Provider"})
        }
)
public class UserOAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OAuthAccountID", nullable = false, updatable = false)
    private UUID oauthAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "Provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "ProviderUserID", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "Email", length = 255)
    private String email;

    @Column(name = "EmailVerified")
    private Boolean emailVerified;

    @Column(name = "DisplayName", length = 255)
    private String displayName;

    @Column(name = "AvatarUrl", length = 1000)
    private String avatarUrl;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
