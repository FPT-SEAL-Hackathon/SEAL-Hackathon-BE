package com.fpt.swp.sealhackathonbe.eventparticipant.repository;

import com.fpt.swp.sealhackathonbe.eventparticipant.entity.EventParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, UUID> {
    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    boolean existsByEventIdAndUserIdAndParticipantStatusStatusNameIgnoreCase(UUID eventId, UUID userId, String statusName);

    @EntityGraph(attributePaths = {
            "participantStatus",
            "event",
            "event.eventStatus",
            "user",
            "user.userType",
            "user.accountStatus",
            "approvedByUser",
            "approvedByUser.userType",
            "approvedByUser.accountStatus"
    })
    Optional<EventParticipant> findByEventIdAndUserId(UUID eventId, UUID userId);

    @EntityGraph(attributePaths = {
            "participantStatus",
            "event",
            "event.eventStatus",
            "user",
            "user.userType",
            "user.accountStatus",
            "approvedByUser",
            "approvedByUser.userType",
            "approvedByUser.accountStatus"
    })
    Optional<EventParticipant> findByEventParticipantId(UUID eventParticipantId);

    @EntityGraph(attributePaths = {
            "participantStatus",
            "event",
            "event.eventStatus",
            "user",
            "user.userType",
            "user.accountStatus",
            "approvedByUser",
            "approvedByUser.userType",
            "approvedByUser.accountStatus"
    })
    List<EventParticipant> findByUserIdOrderByAppliedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {
            "participantStatus"
    })
    List<EventParticipant> findByUserIdAndEventIdIn(UUID userId, List<UUID> eventIds);

    @EntityGraph(attributePaths = {
            "participantStatus",
            "event",
            "event.eventStatus",
            "user",
            "user.userType",
            "user.accountStatus",
            "approvedByUser",
            "approvedByUser.userType",
            "approvedByUser.accountStatus"
    })
    @Query("""
            SELECT ep
            FROM EventParticipant ep
            WHERE (:eventId IS NULL OR ep.eventId = :eventId)
              AND (:ownerUserId IS NULL OR ep.event.createdBy.userId = :ownerUserId)
              AND (:status IS NULL OR LOWER(ep.participantStatus.statusName) = LOWER(:status))
              AND (:categoryId IS NULL OR EXISTS (
                    SELECT 1
                    FROM Category c
                    WHERE c.categoryId = :categoryId
                      AND c.event.eventId = ep.eventId
              ))
              AND (
                    :keyword IS NULL
                    OR LOWER(ep.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(ep.user.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(ep.user.fptStudentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(ep.user.externalStudentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(ep.event.eventName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
              AND (
                    :university IS NULL
                    OR LOWER(ep.user.universityName) LIKE LOWER(CONCAT('%', :university, '%'))
              )
            """)
    Page<EventParticipant> search(
            @Param("eventId") UUID eventId,
            @Param("categoryId") UUID categoryId,
            @Param("ownerUserId") UUID ownerUserId,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("university") String university,
            Pageable pageable
    );
}
