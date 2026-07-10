package com.fpt.swp.sealhackathonbe.criteria.controller;

import com.fpt.swp.sealhackathonbe.criteria.dto.request.CreateTemplateRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.request.UpdateTemplateRequest;
import com.fpt.swp.sealhackathonbe.criteria.dto.response.CriterionTemplateResponse;
import com.fpt.swp.sealhackathonbe.criteria.service.CriterionTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CriterionTemplateController {
    private final CriterionTemplateService templateService;

    @GetMapping("/criteria/templates")
    public List<CriterionTemplateResponse> getAllActiveTemplates() {
        return templateService.getAllActiveCriterionTemplates();
    }

    @GetMapping("/criteria/template/{id}")
    public CriterionTemplateResponse getById(@PathVariable UUID id) {
        return templateService.getById(id);
    }

    @PostMapping("/criteria/template")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<CriterionTemplateResponse> createTemplate(
            @RequestBody CreateTemplateRequest request
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(templateService.create(request));
    }

    @PutMapping("/criteria/template/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<CriterionTemplateResponse> updateTemplate(
            @PathVariable UUID id,
            @RequestBody UpdateTemplateRequest request
            ) {
        return ResponseEntity.ok(templateService.update(id, request));
    }

    @DeleteMapping("/criteria/template/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
