package com.fpt.swp.sealhackathonbe.criteria.service.impl;

import com.fpt.swp.sealhackathonbe.auth.service.mapper.AuthenticationService;
import com.fpt.swp.sealhackathonbe.criteria.dto.request.CreateTemplateRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.request.UpdateTemplateRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.response.CriterionTemplateResponse;
import com.fpt.swp.sealhackathonbe.criteria.entity.CriterionTemplate;
import com.fpt.swp.sealhackathonbe.criteria.repository.CriterionTemplateRepository;
import com.fpt.swp.sealhackathonbe.criteria.service.CriterionTemplateService;
import com.fpt.swp.sealhackathonbe.criteria.service.mapper.Mapper;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriterionTemplateServiceImpl implements CriterionTemplateService {

    private final CriterionTemplateRepository templateRepository;
    private final Mapper mapper;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional(readOnly = true)
    public List<CriterionTemplateResponse> getAllActiveCriterionTemplates() {
        return templateRepository.findAllByIsActiveTrue()
                .stream()
                .map(mapper::toTemplateResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CriterionTemplateResponse getById(UUID templateId) {
        CriterionTemplate criterionTemplate = templateRepository
                .findById(templateId)
                .orElseThrow(() -> new RuntimeException("Criterion template not found"));
        return mapper.toTemplateResponse(criterionTemplate);
    }

    @Override
    @Transactional
    public CriterionTemplateResponse create(CreateTemplateRequest request) {
        CriterionTemplate template = CriterionTemplate.builder()
                .templateId(UUID.randomUUID())
                .criterionName(request.getCriterionName())
                .description(request.getDescription())
                .defaultWeight(request.getDefaultWeight())
                .maxScore(request.getMaxScore())
                .isActive(true)
                .createdBy(authenticationService.getCurrentUser())
                .createdAt(LocalDateTime.now())
                .build();

        return mapper.toTemplateResponse(templateRepository.save(template));
    }

    @Override
    public CriterionTemplateResponse update(UUID templateId, UpdateTemplateRequest request) {
        CriterionTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Criterion template not found"));

        template.setCriterionName(request.getCriterionName());
        template.setDescription(request.getDescription());
        template.setDefaultWeight(request.getDefaultWeight());
        template.setMaxScore(request.getMaxScore());

        return mapper.toTemplateResponse(templateRepository.save(template));
    }

    @Override
    public void delete(UUID templateId) {
        CriterionTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Criterion template not found"));
        template.setIsActive(false);
        templateRepository.save(template);
    }
}
