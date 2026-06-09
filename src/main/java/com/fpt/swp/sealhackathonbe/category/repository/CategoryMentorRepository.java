package com.fpt.swp.sealhackathonbe.category.repository;

import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryMentorRepository extends JpaRepository<CategoryMentor, UUID> {

}
