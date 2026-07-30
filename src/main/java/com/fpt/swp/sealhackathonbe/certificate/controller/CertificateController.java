package com.fpt.swp.sealhackathonbe.certificate.controller;

import com.fpt.swp.sealhackathonbe.certificate.dto.CertificateItemResponse;
import com.fpt.swp.sealhackathonbe.certificate.service.CertificateService;
import com.fpt.swp.sealhackathonbe.user.entity.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificate Management", description = "APIs for generating and downloading certificates")
public class CertificateController {
    private final CertificateService certificateService;

    @Operation(
            summary = "Get available certificates for an event",
            description = "Get list of certificates (Participation and Award) available for the authenticated student in an event.",
            operationId = "getCertificatesForEvent"
    )
    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<CertificateItemResponse>> getCertificatesForEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CertificateItemResponse> list = certificateService.getCertificatesForUserInEvent(
                eventId,
                principal.getUser().getUserId()
        );
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Download participation certificate PDF",
            description = "Generate and download the participation certificate PDF for an authenticated student in an event.",
            operationId = "downloadParticipationCertificatePdf"
    )
    @GetMapping(value = "/download/participation/{eventId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadParticipationCertificate(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        byte[] pdfBytes = certificateService.generateParticipationCertificatePdf(
                eventId,
                principal.getUser().getUserId()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Participation_Certificate_" + eventId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    @Operation(
            summary = "Download award certificate PDF",
            description = "Generate and download the published award certificate PDF for an authenticated student in the awarded team.",
            operationId = "downloadCertificatePdf"
    )
    @GetMapping(value = "/download/{awardId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable UUID awardId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        byte[] pdfBytes = certificateService.generateCertificatePdf(
                awardId,
                principal.getUser().getUserId()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Certificate_" + awardId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
