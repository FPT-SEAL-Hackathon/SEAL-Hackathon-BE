package com.fpt.swp.sealhackathonbe.submission.controller;

import com.fpt.swp.sealhackathonbe.submission.dto.PublicCountResponse;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Submissions", description = "Public APIs for submission statistics")
public class PublicSubmissionController {
    // Public endpoint khong can JWT, chi tra so luong submission tong.

    private final SubmissionsRepository submissionsRepository;

    @Operation(summary = "Count all submissions in the system")
    @GetMapping("/submissions/count")
    public ResponseEntity<PublicCountResponse> countAllSubmissions() {
        return ResponseEntity.ok(new PublicCountResponse(submissionsRepository.count()));
    }
}
