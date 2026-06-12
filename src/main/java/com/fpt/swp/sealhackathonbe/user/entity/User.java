package com.fpt.swp.sealhackathonbe.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

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
@Component
public class User {
    @Id
    @Column(name = "UserID", nullable = false, updatable = false)
    @GeneratedValue
    private UUID userId;

    // Email UNIQUE
    @Column(name = "Email", nullable = false, length = 255)
    private String email;

    @Column(name = "PasswordHash", nullable = false, length = 512)
    private String passwordHash;

    @Column(name = "FullName", nullable = false, length = 200)
    private String fullName;

    @Column(name = "Phone", length = 20)
    private String phone;

    // FK -> UserType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserTypeID", nullable = false)
    private UserType userType;

    // FK -> AccountStatus
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AccountStatusID", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "FPTStudentCode", length = 20)
    private String fptStudentCode;

    @Column(name = "ExternalStudentCode", length = 50)
    private String externalStudentCode;

    @Column(name = "UniversityName", length = 200)
    private String universityName;

    // CreatedAt default GETUTCDATE()
    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    // UpdatedAt default GETUTCDATE()
    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    // FK self reference (duyệt bởi user khác)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovedByUserID")
    private User approvedBy;

    @Column(name = "AccountExpiresAt")
    private LocalDateTime accountExpiresAt;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", userType=" + userType +
                ", accountStatus=" + accountStatus +
                ", fptStudentCode='" + fptStudentCode + '\'' +
                ", externalStudentCode='" + externalStudentCode + '\'' +
                ", universityName='" + universityName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", approvedAt=" + approvedAt +
                ", approvedBy=" + approvedBy +
                ", accountExpiresAt=" + accountExpiresAt +
                ", isDeleted=" + isDeleted +
                '}';
    }
}