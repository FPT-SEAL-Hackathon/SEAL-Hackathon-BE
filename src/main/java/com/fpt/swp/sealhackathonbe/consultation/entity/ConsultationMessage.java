package com.fpt.swp.sealhackathonbe.consultation.entity;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ConsultationMessages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ConsultationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "MessageID")
    private UUID messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RequestID", nullable = false)
    private ConsultationRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SenderID", nullable = false)
    private User sender;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "AttachmentUrl", length = 500)
    private String attachmentUrl;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "SeenAt")
    private LocalDateTime seenAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
