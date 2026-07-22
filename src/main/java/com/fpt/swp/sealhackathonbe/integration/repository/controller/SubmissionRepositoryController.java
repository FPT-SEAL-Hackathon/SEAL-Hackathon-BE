package com.fpt.swp.sealhackathonbe.integration.repository.controller;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.request.ValidateRepositoryRequest;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.SubmissionRepositoryResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionRepositoryController {

    private final SubmissionRepositoryService submissionRepositoryService;

    @PostMapping("/repository/validate")
    public ResponseEntity<SubmissionRepositoryResponse> validateRepository(
            @Valid @RequestBody ValidateRepositoryRequest request) {
        SubmissionRepositoryResponse response = submissionRepositoryService.validateRepositoryUrl(request.getRepositoryUrl());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{submissionId}/repository")
    public ResponseEntity<SubmissionRepositoryResponse> getSubmissionRepository(
            @PathVariable UUID submissionId) {
        SubmissionRepositoryResponse response = submissionRepositoryService.getSubmissionRepository(submissionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{submissionId}/repository/sync")
    public ResponseEntity<SubmissionRepositoryResponse> syncSubmissionRepository(
            @PathVariable UUID submissionId) {
        SubmissionRepositoryResponse response = submissionRepositoryService.syncSubmissionRepository(submissionId);
        return ResponseEntity.ok(response);
    }
}
