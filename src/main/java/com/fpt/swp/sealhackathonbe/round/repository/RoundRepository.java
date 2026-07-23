package com.fpt.swp.sealhackathonbe.round.repository;


import com.fpt.swp.sealhackathonbe.round.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
    boolean existsByCategoryCategoryId(UUID categoryId);

    List<Round> findByCategoryCategoryIdOrderByRoundOrderAsc(UUID categoryId);

    //Find max roundOrder by Category
    @Query("SELECT COALESCE(MAX(r.roundOrder), 0) FROM Round r WHERE r.category.categoryId = :categoryId")
    Integer findMaxRoundOrderByCategory(@Param("categoryId") UUID categoryId);

    //Find final round
    Optional<Round> findTopByCategoryCategoryIdOrderByRoundOrderDesc(UUID categoryId);

    Optional<Round> findTopByCategoryCategoryIdAndRoundOrderLessThanOrderByRoundOrderDesc(
            UUID categoryId,
            Integer roundOrder
    );

    boolean existsByCategoryCategoryIdAndRoundNameIgnoreCase(UUID categoryId, String roundName);

    @Query("SELECT COUNT(r) FROM Round r WHERE r.category.event.eventId = :eventId")
    long countByEventId(@Param("eventId") UUID eventId);

    @Query(value = "SELECT COUNT(*) FROM dbo.CalibrationSamples WHERE RoundID = :roundId", nativeQuery = true)
    long countCalibrationSamplesByRoundId(@Param("roundId") UUID roundId);

    // Repository metadata: tra eventId truc tiep bang JPQL de tranh LazyInitializationException
    // khi caller (orchestration ngoai transaction) can xac dinh event cua submission qua round.
    @Query("SELECT r.category.event.eventId FROM Round r WHERE r.roundId = :roundId")
    Optional<UUID> findEventIdByRoundId(@Param("roundId") UUID roundId);

}
