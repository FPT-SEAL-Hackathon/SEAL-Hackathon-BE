package com.fpt.swp.sealhackathonbe.criteria.repository;

import com.fpt.swp.sealhackathonbe.criteria.entity.CriterionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CriterionTemplateRespository extends JpaRepository<CriterionTemplate, UUID> {

}
