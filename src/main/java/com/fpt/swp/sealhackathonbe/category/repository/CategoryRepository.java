package com.fpt.swp.sealhackathonbe.category.repository;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByEvent_EventId(UUID eventId);

}
