package com.fpt.swp.sealhackathonbe.category.repository;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByEventEventId(UUID eventId);
    List<Category> findByEventEventIdAndIsActiveTrueOrderBySortOrderAsc(UUID eventId);

    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM Category c WHERE c.event.eventId = :eventId")
    Integer findMaxSortOrderByEventEventId(@Param("eventId") UUID eventId);

    boolean existsByEventEventIdAndSortOrder(UUID eventId, Integer sortOrder);
    boolean existsByEventEventIdAndCategoryNameAndIsActiveTrue(UUID eventId, String categoryName);

}
