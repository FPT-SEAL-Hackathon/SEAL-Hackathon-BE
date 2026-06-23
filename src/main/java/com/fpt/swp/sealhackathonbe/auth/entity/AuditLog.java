package com.fpt.swp.sealhackathonbe.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "AuditLog")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "LogID", nullable = false)
    private UUID id;

    @Column(name = "ActionType", nullable = false, length = 100)
    private String actionType;

    @Column(name = "EntityType", nullable = false, length = 100)
    private String entityType;

    @Column(name = "EntityID")
    private UUID entityId;

    @Column(name = "EntityKey", length = 200)
    private String entityKey;

    @Column(name = "ActorUserID")
    private UUID actorUserId;

    @Column(name = "OldValueJSON")
    private String oldValueJson;

    @Column(name = "NewValueJSON")
    private String newValueJson;

    @Column(name = "IPAddress", length = 50)
    private String ipAddress;

    @Column(name = "OccurredAt", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "Notes")
    private String notes;

}
