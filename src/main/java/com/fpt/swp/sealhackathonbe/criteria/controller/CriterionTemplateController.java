package com.fpt.swp.sealhackathonbe.criteria.controller;

import com.fpt.swp.sealhackathonbe.criteria.dto.response.CriterionTemplateResponse;
import com.fpt.swp.sealhackathonbe.criteria.service.CriterionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/criteria/templates")
@RequiredArgsConstructor
public class CriterionTemplateController {
    private final CriterionTemplateService templateService;

    @GetMapping
    public List<CriterionTemplateResponse> getAllActiveTemplates() {
        return templateService.getAllActiveCriterionTemplates();
    }

    @GetMapping("{id}")
    public CriterionTemplateResponse getById(@PathVariable UUID id) {
        return templateService.getById(id);
    }
}
