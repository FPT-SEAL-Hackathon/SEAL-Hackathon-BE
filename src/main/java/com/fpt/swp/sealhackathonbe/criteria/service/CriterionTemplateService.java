package com.fpt.swp.sealhackathonbe.criteria.service;

import com.fpt.swp.sealhackathonbe.criteria.dto.request.CreateTemplateRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.request.UpdateTemplateRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.response.CriterionTemplateResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CriterionTemplateService {
    List<CriterionTemplateResponse> getAllActiveCriterionTemplates();
    CriterionTemplateResponse getById(UUID templateId);
    CriterionTemplateResponse create(CreateTemplateRequest request);
    CriterionTemplateResponse update(UUID templateId, UpdateTemplateRequest request);
    void delete(UUID templateId);
}
