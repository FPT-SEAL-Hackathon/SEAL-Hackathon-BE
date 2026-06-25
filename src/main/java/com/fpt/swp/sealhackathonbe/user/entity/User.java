package com.fpt.swp.sealhackathonbe.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Đại diện tài khoản, hồ sơ và role dùng cho xác thực/phân quyền.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "Users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "Email")
        }
)
public class User {
    @Id
    @Column(name = "UserID", nullable = false, updatable = false)
    @GeneratedValue
    private UUID userId;

    @Column(name = "Email", nullable = false, length = 255)
    private String email;

    @Column(name = "PasswordHash", nullable = false, length = 512)
    private String passwordHash;

    @Column(name = "FullName", nullable = false, length = 200)
    private String fullName;

    @Column(name = "Phone", length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserTypeID", nullable = false)
    private UserType userType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountStatusID", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "FPTStudentCode", length = 20)
    private String fptStudentCode;

    @Column(name = "ExternalStudentCode", length = 50)
    private String externalStudentCode;

    @Column(name = "UniversityName", length = 200)
    private String universityName;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedByUserID")
    private User approvedBy;

    @Column(name = "AccountExpiresAt")
    private LocalDateTime accountExpiresAt;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * RBAC:
     * UserType là nguồn để suy ra role bảo mật của tài khoản.
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * RBAC:
     * Gán loại user khi đăng ký hoặc khi admin cập nhật quyền.
     */
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    /**
     * Không đưa password hoặc quan hệ lazy vào log/debug.
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
