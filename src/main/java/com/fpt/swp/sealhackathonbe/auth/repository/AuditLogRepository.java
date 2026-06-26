package com.fpt.swp.sealhackathonbe.auth.repository;

import com.fpt.swp.sealhackathonbe.auth.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Truy cập dữ liệu audit log cho các thao tác nghiệp vụ và bảo mật.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
