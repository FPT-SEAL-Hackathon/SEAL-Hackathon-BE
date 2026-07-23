package com.fpt.swp.sealhackathonbe.integration.repository.controller;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.response.EventSubmissionRepositoryItemResponse;
import com.fpt.swp.sealhackathonbe.integration.repository.service.SubmissionRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Organizer (creator cua event) xem tong quan repository cua moi submission trong event.
 * Quyen duoc check trong service (creator-only, giong legacy Event-level integration).
 */
@RestController
@RequestMapping("/api/v1/events/{eventId}/submission-repositories")
@RequiredArgsConstructor
public class EventSubmissionRepositoryController {

    private final SubmissionRepositoryService submissionRepositoryService;

    @GetMapping
    public ResponseEntity<List<EventSubmissionRepositoryItemResponse>> getEventSubmissionRepositories(
            @PathVariable UUID eventId) {
        return ResponseEntity.ok(submissionRepositoryService.getEventSubmissionRepositories(eventId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportEventSubmissionRepositoriesCsv(@PathVariable UUID eventId) {
        String csv = submissionRepositoryService.exportEventSubmissionRepositoriesCsv(eventId);
        // BOM UTF-8 de Excel mo file co ky tu tieng Viet khong bi vo font.
        byte[] body = ("﻿" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"submission-repositories-" + eventId + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}
