package com.fpt.swp.sealhackathonbe.auth.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
 * Vé liên kết tài khoản ngắn hạn (một lần, hết hạn 15 phút):
 * - GOOGLE_LINK: gắn định danh Google (đã xác thực) vào user hiện có cùng email,
 *   sau khi chủ tài khoản xác minh bằng mật khẩu local hoặc OTP email.
 * - LOCAL_SETUP: thiết lập mật khẩu local cho user Google-only hiện có,
 *   sau khi xác minh OTP email.
 * Chỉ lưu HASH của linkingToken và OTP — không lưu giá trị thô.
 * User đích được resolve ở server; frontend không được chỉ định UserID.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "AccountLinkTickets")
public class AccountLinkTicket {

    public static final String PURPOSE_GOOGLE_LINK = "GOOGLE_LINK";
    public static final String PURPOSE_LOCAL_SETUP = "LOCAL_SETUP";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TicketID", nullable = false, updatable = false)
    private UUID ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "Purpose", nullable = false, length = 30)
    private String purpose;

    @Column(name = "Provider", length = 30)
    private String provider;

    @Column(name = "ProviderUserID", length = 255)
    private String providerUserId;

    @Column(name = "ProviderEmail", length = 255)
    private String providerEmail;

    @Column(name = "ProviderEmailVerified")
    private Boolean providerEmailVerified;

    @Column(name = "ProviderDisplayName", length = 255)
    private String providerDisplayName;

    @Column(name = "ProviderAvatarUrl", length = 1000)
    private String providerAvatarUrl;

    @Column(name = "TokenHash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "OtpHash", length = 128)
    private String otpHash;

    @Column(name = "OtpExpiresAt")
    private LocalDateTime otpExpiresAt;

    @Column(name = "OtpAttempts", nullable = false)
    @Builder.Default
    private Integer otpAttempts = 0;

    @Column(name = "OtpLastSentAt")
    private LocalDateTime otpLastSentAt;

    @Column(name = "OtpVerifiedAt")
    private LocalDateTime otpVerifiedAt;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "ConsumedAt")
    private LocalDateTime consumedAt;
}
