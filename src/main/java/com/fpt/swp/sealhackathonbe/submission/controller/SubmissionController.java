package com.fpt.swp.sealhackathonbe.submission.controller;

import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifySubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionCommandService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionDisqualificationService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final SubmissionDisqualificationService submissionDisqualificationService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Submit work",
            description = "Submit or update a team's work for a round. This API calls sp_UpsertSubmission."
    )
    @PostMapping("/submissions")
    public ResponseEntity<SubmissionResponse> submitWork(
            @Valid @RequestBody CreateSubmissionRequest request,
            Authentication authentication
    ) {
        SubmissionResponse response =
                submissionCommandService.submitWork(request, currentUserId(authentication));

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
        // Luong doc: API nhan submissionId, chuyen viec tim kiem cho query service,
        // sau do tra ve DTO thay vi expose truc tiep JPA entity.
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
        // Luong doc: teamId + roundId xac dinh duy nhat mot submission,
        // duoc rang buoc boi UQ_Submissions_Team_Round trong entity Submissions.
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
        // Luong doc: roundId loc tat ca submission cua mot round cho man hinh judge/admin.
        List<SubmissionResponse> response =
                submissionQueryService.getSubmissionsByRound(roundId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get submissions by event",
            description = "Get all submissions belonging to teams in a specific event."
    )
    @GetMapping("/events/{eventId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> findByEventId(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(submissionQueryService.findByEventId(eventId));
    }

    @Operation(
            summary = "Disqualify submission",
            description = "Disqualify a single submission without disqualifying the whole team."
    )
    @PostMapping("/admin/submissions/{submissionId}/disqualify")
    public ResponseEntity<SubmissionDisqualificationResponse> disqualifySubmission(
            @PathVariable UUID submissionId,
            @Valid @RequestBody DisqualifySubmissionRequest request,
            Authentication authentication
    ) {
        // Endpoint rieng cho submission de khong nham voi /admin/teams/{teamId}/disqualify.
        // Service se ghi Disqualifications.SubmissionID va de TeamID = null.
        SubmissionDisqualificationResponse response =
                submissionDisqualificationService.disqualifySubmission(
                        submissionId,
                        request,
                        currentUserId(authentication)
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get disqualified submissions",
            description = "Get all submissions with an active disqualification, newest first."
    )
    @GetMapping("/admin/submissions/disqualified")
    public ResponseEntity<List<DisqualifiedSubmissionResponse>> getDisqualifiedSubmissions() {
        return ResponseEntity.ok(
                submissionDisqualificationService.getDisqualifiedSubmissions()
        );
    }

    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Unauthenticated user");
        }

        User user = userRepository.findByEmail(authentication.getName());
        if (user == null) {
            throw new RuntimeException("Authenticated user not found");
        }

        return user.getUserId();
    }
}
