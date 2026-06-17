package com.fpt.swp.sealhackathonbe.studentdownload.controller;

import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;
import com.fpt.swp.sealhackathonbe.studentdownload.service.StudentDownloadService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/student-downloads")
@RequiredArgsConstructor
@Tag(name = "Student Downloads", description = "APIs for students to download round problems")
public class StudentDownloadController {
    private final StudentDownloadService studentDownloadService;

    @Operation(
            summary = "Download round problem CSV",
            description = "Download round problem information as a UTF-8 CSV file for an authenticated student in the same event category.",
            operationId = "downloadRoundProblemCsv"
    )
    @GetMapping(value = "/rounds/{roundId}/problem-csv", produces = "text/csv")
    public ResponseEntity<byte[]> downloadRoundProblemCsv(
            @PathVariable UUID roundId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        DownloadFileResponse file = studentDownloadService.downloadRoundProblemCsv(
                roundId,
                principal.getUser().getUserId()
        );
        return buildDownloadResponse(file);
    }

    private ResponseEntity<byte[]> buildDownloadResponse(DownloadFileResponse file) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getContent().length)
                .body(file.getContent());
    }
}
