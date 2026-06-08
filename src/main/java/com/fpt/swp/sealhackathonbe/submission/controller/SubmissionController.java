package com.fpt.swp.sealhackathonbe.submission.controller;

import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionCommandService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Submission Management",
        description = "APIs for submitting work and viewing submissions"
)
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionCommandService submissionCommandService;
    private final SubmissionQueryService submissionQueryService;

    @Operation(
            summary = "Submit work",
            description = "Submit or update a team's work for a round. This API calls sp_UpsertSubmission."
    )
    @PostMapping("/submissions")
    public ResponseEntity<SubmissionResponse> submitWork(
            @Valid @RequestBody CreateSubmissionRequest request
    ) {
        UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        SubmissionResponse response =
                submissionCommandService.submitWork(request, currentUserId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get submission by ID",
            description = "Get detail of a single submission by submission ID."
    )
    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<SubmissionResponse> getSubmissionById(
            @PathVariable UUID submissionId
    ) {
        SubmissionResponse response =
                submissionQueryService.getSubmissionById(submissionId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get team submission in round",
            description = "Get the submission of a specific team in a specific round."
    )
    @GetMapping("/teams/{teamId}/rounds/{roundId}/submission")
    public ResponseEntity<SubmissionResponse> getSubmissionByTeamAndRound(
            @PathVariable UUID teamId,
            @PathVariable UUID roundId
    ) {
        SubmissionResponse response =
                submissionQueryService.getSubmissionByTeamAndRound(teamId, roundId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get submissions by round",
            description = "Get all submissions submitted in a specific round."
    )
    @GetMapping("/rounds/{roundId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByRound(
            @PathVariable UUID roundId
    ) {
        List<SubmissionResponse> response =
                submissionQueryService.getSubmissionsByRound(roundId);

        return ResponseEntity.ok(response);
    }
}
