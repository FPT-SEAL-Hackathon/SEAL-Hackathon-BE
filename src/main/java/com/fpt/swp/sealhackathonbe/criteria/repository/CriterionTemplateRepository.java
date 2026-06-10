package com.fpt.swp.sealhackathonbe.criteria.repository;

import com.fpt.swp.sealhackathonbe.criteria.entity.CriterionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CriterionTemplateRepository extends JpaRepository<CriterionTemplate, UUID> {
    List<CriterionTemplate> findAllByIsActiveTrue();
}
