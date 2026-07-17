package com.fpt.swp.sealhackathonbe.auth.repository;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Truy cập dữ liệu audit log cho các thao tác nghiệp vụ và bảo mật.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // Hard delete user: giữ lại audit log nhưng gỡ tham chiếu actor
    // (ActorUserID có FK tới Users, cột nullable).
    @Modifying
    @Query("UPDATE AuditLog a SET a.actorUserId = NULL WHERE a.actorUserId = :userId")
    int clearActor(@Param("userId") UUID userId);
}
