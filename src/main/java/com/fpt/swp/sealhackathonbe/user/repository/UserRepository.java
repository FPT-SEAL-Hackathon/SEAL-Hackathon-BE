package com.fpt.swp.sealhackathonbe.user.repository;

import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy cập tài khoản phục vụ xác thực và quản lý người dùng.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * RBAC:
     * Nạp kèm role/status để quyết định xác thực và phân quyền.
     */
    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    User findByEmail(String email);

    /**
     * Kiểm tra email đã được đăng ký để tránh trùng tài khoản.
     */
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    Optional<User> findByUserIdAndIsDeletedFalse(UUID userId);

    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE (u.isDeleted = false OR u.isDeleted IS NULL)
              AND u.userType.typeName IN :typeNames
            """)
    Long countActiveUsersByTypeNames(@Param("typeNames") Collection<String> typeNames);

    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    @Query("""
            SELECT u
            FROM User u
            WHERE (u.isDeleted = false OR u.isDeleted IS NULL)
              AND (
                    :search IS NULL
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.fptStudentCode) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.externalStudentCode) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(u.universityName) LIKE LOWER(CONCAT('%', :search, '%'))
              )
              AND (:role IS NULL OR LOWER(u.userType.typeName) = LOWER(:role))
              AND (:status IS NULL OR LOWER(u.accountStatus.statusName) = LOWER(:status))
              AND (:joinedFrom IS NULL OR u.createdAt >= :joinedFrom)
              AND (:joinedTo IS NULL OR u.createdAt <= :joinedTo)
              AND (
                    :teamId IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM TeamMembers tm
                        WHERE tm.userId = u.userId
                          AND tm.active = true
                          AND tm.teamId = :teamId
                    )
              )
              AND (
                    :teamName IS NULL
                    OR EXISTS (
                        SELECT 1
                        FROM TeamMembers tm
                        WHERE tm.userId = u.userId
                          AND tm.active = true
                          AND LOWER(tm.team.teamName) LIKE LOWER(CONCAT('%', :teamName, '%'))
                    )
              )
            """)
    Page<User> searchForManagement(
            @Param("search") String search,
            @Param("role") String role,
            @Param("teamId") UUID teamId,
            @Param("teamName") String teamName,
            @Param("status") String status,
            @Param("joinedFrom") LocalDateTime joinedFrom,
            @Param("joinedTo") LocalDateTime joinedTo,
            Pageable pageable
    );
}
