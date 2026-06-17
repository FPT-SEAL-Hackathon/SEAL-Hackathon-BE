package com.fpt.swp.sealhackathonbe.category.repository;

import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryMentorRepository extends JpaRepository<CategoryMentor, UUID> {
    @Query("SELECT cm.mentor.userId FROM CategoryMentor cm WHERE cm.category.categoryId = :categoryId")
    List<UUID> findMentorIdsByCategoryId(@Param("categoryId") UUID categoryId);

}
