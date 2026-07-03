package com.fpt.swp.sealhackathonbe.submission.controller;

import com.fpt.swp.sealhackathonbe.auth.service.impl.AuthenticationServiceImpl;
import com.fpt.swp.sealhackathonbe.submission.dto.CreateSubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifySubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionCommandService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionDisqualificationService;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    // Controller nhan HTTP request, lay user hien tai va chuyen nghiep vu cho tung service chuyen trach.
    private final SubmissionCommandService submissionCommandService;
    private final SubmissionQueryService submissionQueryService;
    private final SubmissionDisqualificationService submissionDisqualificationService;
    private final AuthenticationServiceImpl authenticationServiceImpl;

    @Operation(
            summary = "Submit work",
            description = "Submit or update a team's work for a round. This API calls sp_UpsertSubmission."
    )
    @PostMapping("/submissions")
    public ResponseEntity<SubmissionResponse> submitWork(
            @Valid @RequestBody CreateSubmissionRequest request
    ) {
        // Du lieu: request + user dang dang nhap -> command service -> stored procedure -> response.
        SubmissionResponse response =
                submissionCommandService.submitWork(request, currentUserId());

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
                submissionQueryService.getSubmissionByTeamAndRound(
                        teamId,
                        roundId,
                        currentUserId()
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get submissions by round",
            description = "Get all submissions in one round. Use an organizer account."
    )
    @GetMapping("/admin/rounds/{roundId}/submissions")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByRound(
            @PathVariable UUID roundId
    ) {
        List<SubmissionResponse> response = submissionQueryService.getSubmissionsByRound(roundId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get unreview submissions by round",
            description = "Get submissions in one round that have not been scored yet."
    )
    @GetMapping("/admin/rounds/{roundId}/unreview-submissions")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER','ROLE_INTERNAL_JUDGE', 'ROLE_GUEST_JUDGE', 'ROLE_EXPERT')")
    public ResponseEntity<List<SubmissionResponse>> getUnreviewSubmissionByRound(
            @PathVariable UUID roundId
    ) {
        List<SubmissionResponse> response = submissionQueryService.getUnreviewSubmissionByRound(roundId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get submissions by event",
            description = "Get all submissions from teams in one event. Use an organizer account."
    )
    @GetMapping("/admin/events/{eventId}/submissions")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByEvent(
            @PathVariable UUID eventId
    ) {
        List<SubmissionResponse> response = submissionQueryService.findByEventId(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Disqualify a submission",
            description = "Mark one submission as disqualified and record the reason. Use an organizer account."
    )
    @PostMapping("/admin/submissions/{submissionId}/disqualify")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<SubmissionDisqualificationResponse> disqualifySubmission(
            @PathVariable UUID submissionId,
            @Valid @RequestBody DisqualifySubmissionRequest request
    ) {
        SubmissionDisqualificationResponse response =
                submissionDisqualificationService.disqualifySubmission(
                        submissionId,
                        request,
                        currentUserId()
                );

        return ResponseEntity.ok(response);
    }

    private UUID currentUserId() {
        return authenticationServiceImpl.getCurrentUser().getUserId();
    }
}
