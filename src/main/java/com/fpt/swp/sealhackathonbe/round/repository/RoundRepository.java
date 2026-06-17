package com.fpt.swp.sealhackathonbe.round.repository;


import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
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
    List<Round> findByCategoryCategoryIdOrderByRoundOrderAsc(UUID categoryId);

    //Find max roundOrder by Category
    @Query("SELECT COALESCE(MAX(r.roundOrder), 0) FROM Round r WHERE r.category.categoryId = :categoryId")
    Integer findMaxRoundOrderByCategory(@Param("categoryId") UUID categoryId);

    //Find final round
    Optional<Round> findTopByCategoryCategoryIdOrderByRoundOrderDesc(UUID categoryId);

}
