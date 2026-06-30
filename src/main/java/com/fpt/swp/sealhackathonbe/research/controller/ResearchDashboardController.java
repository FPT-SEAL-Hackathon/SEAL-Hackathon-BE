package com.fpt.swp.sealhackathonbe.research.controller;

import com.fpt.swp.sealhackathonbe.research.service.ResearchDataService;
import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/research")
@RequiredArgsConstructor
@Tag(name = "Research Dashboard Controller", description = "APIs for research analytics and score quality metrics")
public class ResearchDashboardController {
    private final ResearchDataService researchDataService;

    @GetMapping(value = "/events/{eventId}/export", produces = "text/csv")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    @Operation(summary = "Export research data CSV", description = "Exports dashboard, variance report, score distribution, or reliability metrics and writes a DataExportLog record")
    public ResponseEntity<byte[]> exportResearchData(
            @PathVariable UUID eventId,
            @RequestParam(required = false) UUID roundId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal bucketSize,
            @RequestParam(defaultValue = "dashboard") String type,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DownloadFileResponse file = researchDataService.exportResearchData(
                eventId,
                roundId,
                categoryId,
                bucketSize,
                type,
                principal.getUser().getUserId()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getContent().length)
                .body(file.getContent());
    }
}
