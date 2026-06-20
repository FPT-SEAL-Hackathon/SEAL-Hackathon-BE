package com.fpt.swp.sealhackathonbe.criteria.service.impl;

import com.fpt.swp.sealhackathonbe.criteria.dto.response.CriterionTemplateResponse;
import com.fpt.swp.sealhackathonbe.criteria.entity.CriterionTemplate;
import com.fpt.swp.sealhackathonbe.criteria.repository.CriterionTemplateRepository;
import com.fpt.swp.sealhackathonbe.criteria.service.CriterionTemplateService;
import com.fpt.swp.sealhackathonbe.criteria.service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriterionTemplateServiceImpl implements CriterionTemplateService {

    private final CriterionTemplateRepository templateRepository;
    private final Mapper mapper;

    @Override
    public List<CriterionTemplateResponse> getAllActiveCriterionTemplates() {
        return templateRepository.findAllByIsActiveTrue()
                .stream()
                .map(mapper::toTemplateResponse)
                .toList();
    }

    @Override
    public CriterionTemplateResponse getById(UUID templateId) {
        CriterionTemplate criterionTemplate = templateRepository
                .findById(templateId)
                .orElseThrow(() -> new RuntimeException("Criterion template not found"));
        return mapper.toTemplateResponse(criterionTemplate);
    }
}
