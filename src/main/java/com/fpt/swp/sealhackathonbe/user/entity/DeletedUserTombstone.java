package com.fpt.swp.sealhackathonbe.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dấu vết tạm (7 ngày) của tài khoản đã bị xóa cứng: chỉ để hiển thị
 * thông báo "tài khoản đã bị gỡ, hãy tạo tài khoản mới" khi đăng nhập,
 * KHÔNG chặn đăng ký lại bằng email này.
 * DeletedByUserID là UUID thô, không FK — organizer xóa sau này cũng có thể bị xóa.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DeletedUserTombstones")
public class DeletedUserTombstone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "TombstoneID", nullable = false)
    private UUID tombstoneId;

    @Column(name = "Email", nullable = false, length = 255)
    private String email;

    @Column(name = "FullName", length = 200)
    private String fullName;

    @Column(name = "DeletedByUserID")
    private UUID deletedByUserId;

    @Column(name = "Reason", length = 500)
    private String reason;

    @Column(name = "DeletedAt", nullable = false)
    private LocalDateTime deletedAt;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;
}
