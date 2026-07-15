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
import java.util.List;
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
     * Email không còn UNIQUE toàn cục (local + OAuth có thể trùng email),
     * nên phương thức này được định nghĩa TẤT ĐỊNH: ưu tiên tài khoản
     * local-enabled, chưa xóa, tạo sớm nhất.
     */
    default User findByEmail(String email) {
        return findFirstByEmailAndIsDeletedFalseOrderByLocalLoginEnabledDescCreatedAtAsc(email)
                .orElse(null);
    }

    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    Optional<User> findFirstByEmailAndIsDeletedFalseOrderByLocalLoginEnabledDescCreatedAtAsc(String email);

    /**
     * Tìm đúng tài khoản LOCAL (đăng nhập được bằng mật khẩu) theo email.
     */
    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    Optional<User> findFirstByEmailAndLocalLoginEnabledTrueAndIsDeletedFalseOrderByCreatedAtAsc(String email);

    /**
     * Kiểm tra email đã được đăng ký để tránh trùng tài khoản.
     */
    boolean existsByEmail(String email);

    List<User> findByEmailAndIsDeletedFalse(String email);

    // Phát hiện hồ sơ trùng khi complete-profile (loại trừ chính user hiện tại).
    boolean existsByEmailAndIsDeletedFalseAndUserIdNot(String email, UUID userId);

    boolean existsByPhoneAndIsDeletedFalseAndUserIdNot(String phone, UUID userId);

    boolean existsByFptStudentCodeAndIsDeletedFalseAndUserIdNot(String fptStudentCode, UUID userId);

    boolean existsByExternalStudentCodeAndIsDeletedFalseAndUserIdNot(String externalStudentCode, UUID userId);

    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    Optional<User> findByUserIdAndIsDeletedFalse(UUID userId);

    @EntityGraph(attributePaths = {"userType", "accountStatus"})
    @Query("""
            SELECT u FROM User u 
            WHERE (u.isDeleted = false OR u.isDeleted IS NULL)
              AND (
                  LOWER(u.userType.typeName) LIKE '%judge%' OR 
                  LOWER(u.userType.typeName) LIKE '%mentor%' OR 
                  LOWER(u.userType.typeName) LIKE '%expert%'
              )
            """)
    List<User> findExpertsMentorsJudges();

    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE (u.isDeleted = false OR u.isDeleted IS NULL)
              AND u.userType.typeName IN :typeNames
            """)
    Long countActiveUsersByTypeNames(@Param("typeNames") Collection<String> typeNames);

    // Filter đa giá trị: OR trong nhóm (IN), AND giữa các nhóm.
    // JPQL không kiểm tra list rỗng trực tiếp được nên dùng cờ *Empty đi kèm;
    // khi list rỗng caller truyền placeholder để IN (...) hợp lệ cú pháp.
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
              AND (:rolesEmpty = true OR LOWER(u.userType.typeName) IN :roles)
              AND (:statusesEmpty = true OR LOWER(u.accountStatus.statusName) IN :statuses)
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
            @Param("roles") Collection<String> roles,
            @Param("rolesEmpty") boolean rolesEmpty,
            @Param("teamId") UUID teamId,
            @Param("teamName") String teamName,
            @Param("statuses") Collection<String> statuses,
            @Param("statusesEmpty") boolean statusesEmpty,
            @Param("joinedFrom") LocalDateTime joinedFrom,
            @Param("joinedTo") LocalDateTime joinedTo,
            Pageable pageable
    );

    // Facet count kiểu drill-down cho nhóm ROLE: áp mọi filter TRỪ role.
    @Query("""
            SELECT u.userType.typeName, COUNT(u)
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
              AND (:statusesEmpty = true OR LOWER(u.accountStatus.statusName) IN :statuses)
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
            GROUP BY u.userType.typeName
            """)
    List<Object[]> countByRoleForManagement(
            @Param("search") String search,
            @Param("teamId") UUID teamId,
            @Param("teamName") String teamName,
            @Param("statuses") Collection<String> statuses,
            @Param("statusesEmpty") boolean statusesEmpty,
            @Param("joinedFrom") LocalDateTime joinedFrom,
            @Param("joinedTo") LocalDateTime joinedTo
    );

    // Facet count kiểu drill-down cho nhóm STATUS: áp mọi filter TRỪ status.
    @Query("""
            SELECT u.accountStatus.statusName, COUNT(u)
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
              AND (:rolesEmpty = true OR LOWER(u.userType.typeName) IN :roles)
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
            GROUP BY u.accountStatus.statusName
            """)
    List<Object[]> countByStatusForManagement(
            @Param("search") String search,
            @Param("roles") Collection<String> roles,
            @Param("rolesEmpty") boolean rolesEmpty,
            @Param("teamId") UUID teamId,
            @Param("teamName") String teamName,
            @Param("joinedFrom") LocalDateTime joinedFrom,
            @Param("joinedTo") LocalDateTime joinedTo
    );
}
