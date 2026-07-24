package com.fpt.swp.sealhackathonbe.integration.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "RepositoryIntegrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepositoryIntegration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "IntegrationID")
    private UUID integrationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EventID", nullable = false)
    private Event event;

    @Column(name = "Provider", nullable = false)
    private String provider;

    @JsonIgnore
    @Column(name = "EncryptedToken", columnDefinition = "VARBINARY(MAX)")
    private byte[] encryptedToken;

    @JsonIgnore
    @Column(name = "EncryptionFormatVersion")
    private Integer encryptionFormatVersion;

    @JsonIgnore
    @Column(name = "EncryptionIV", columnDefinition = "VARBINARY(12)")
    private byte[] encryptionIv;

    @Column(name = "ConnectionStatus", nullable = false)
    private String connectionStatus; // 'CONNECTED', 'DISCONNECTED', 'TOKEN_INVALID'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByID", nullable = false)
    private User createdBy;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (connectionStatus == null) {
            connectionStatus = "CONNECTED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
