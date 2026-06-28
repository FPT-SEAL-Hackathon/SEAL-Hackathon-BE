package com.fpt.swp.sealhackathonbe.award.repository;

import com.fpt.swp.sealhackathonbe.award.entity.Award;
import com.fpt.swp.sealhackathonbe.award.dto.AwardPrizeTotalResponse;
import com.fpt.swp.sealhackathonbe.award.dto.EventPrizeTotalRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AwardRepository extends JpaRepository<Award, UUID> {
    List<Award> findAllByEventEventId(UUID eventId);
    List<Award> findByIsPublishedTrueOrderByAwardedAtDesc();
    Optional<Award> findByIdAndIsPublishedTrue(UUID id);

    @Query("""
            select new com.fpt.swp.sealhackathonbe.award.dto.AwardPrizeTotalResponse(
                coalesce(a.prizeCurrency, 'VND'),
                sum(a.prizeValue)
            )
            from Award a
            where a.event.eventId = :eventId
            group by coalesce(a.prizeCurrency, 'VND')
            """)
    List<AwardPrizeTotalResponse> sumPrizeByEventId(@Param("eventId") UUID eventId);

    @Query("""
            select new com.fpt.swp.sealhackathonbe.award.dto.AwardPrizeTotalResponse(
                coalesce(a.prizeCurrency, 'VND'),
                sum(a.prizeValue)
            )
            from Award a
            where a.event.isDeleted = false
            group by coalesce(a.prizeCurrency, 'VND')
            """)
    List<AwardPrizeTotalResponse> sumPrizeForAllActiveEvents();

    @Query("""
            select new com.fpt.swp.sealhackathonbe.award.dto.EventPrizeTotalRow(
                a.event.eventId,
                a.event.eventName,
                coalesce(a.prizeCurrency, 'VND'),
                sum(a.prizeValue)
            )
            from Award a
            where a.event.isDeleted = false
            group by a.event.eventId, a.event.eventName, coalesce(a.prizeCurrency, 'VND')
            """)
    List<EventPrizeTotalRow> sumPrizeGroupedByActiveEvent();

    @Query("""
            select count(a) > 0
            from Award a
            where a.event.eventId = :eventId
              and (
                    (:categoryId is null and a.category is null)
                    or (:categoryId is not null and a.category.categoryId = :categoryId)
                  )
              and a.awardTier.id = :awardTierId
              and a.isPublished = true
            """)
    boolean existsPublishedAwardTierInScope(
            @Param("eventId") UUID eventId,
            @Param("categoryId") UUID categoryId,
            @Param("awardTierId") UUID awardTierId
    );
}
