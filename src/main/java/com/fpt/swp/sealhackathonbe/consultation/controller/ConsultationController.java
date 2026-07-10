package com.fpt.swp.sealhackathonbe.consultation.controller;

import com.fpt.swp.sealhackathonbe.consultation.dto.*;
import com.fpt.swp.sealhackathonbe.consultation.service.ConsultationService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Expert Consultation", description = "APIs for Expert Consultation System")
@SecurityRequirement(name = "bearerAuth")
public class ConsultationController {

    private final ConsultationService consultationService;
    private final UserRepository userRepository;

    private User getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        User user = userRepository.findByEmail(auth.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        return user;
    }

    // ==========================================
    // Event Coordinator APIs
    // ==========================================
    
    @PostMapping("/categories/{categoryId}/experts/{expertId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Assign expert to category")
    public ResponseEntity<Void> assignExpertToCategory(@PathVariable UUID categoryId, @PathVariable UUID expertId) {
        consultationService.assignMentorToCategory(categoryId, expertId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/categories/{categoryId}/experts/{expertId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Remove expert from category")
    public ResponseEntity<Void> removeExpertFromCategory(@PathVariable UUID categoryId, @PathVariable UUID expertId) {
        consultationService.removeMentorFromCategory(categoryId, expertId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories/{categoryId}/experts")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'MENTOR', 'EXPERT', 'FPT_STUDENT', 'EXTERNAL_STUDENT')")
    @Operation(summary = "Get experts of category")
    public ResponseEntity<List<MentorProfileResponse>> getExpertsOfCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(consultationService.getMentorsOfCategory(categoryId));
    }

    // ==========================================
    // Expert APIs
    // ==========================================

    @GetMapping("/expert/categories")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT')")
    @Operation(summary = "Get assigned categories for expert")
    public ResponseEntity<List<AssignedCategoryResponse>> getAssignedCategoriesForExpert(Authentication auth) {
        return ResponseEntity.ok(consultationService.getAssignedCategoriesForMentor(getCurrentUser(auth)));
    }

    @GetMapping("/expert/categories/{categoryId}/teams")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT')")
    @Operation(summary = "Get all teams in a category assigned to the expert")
    public ResponseEntity<List<TeamSummaryForMentorResponse>> getTeamsForExpertCategory(
            Authentication auth, @PathVariable UUID categoryId) {
        return ResponseEntity.ok(consultationService.getTeamsForMentorCategory(getCurrentUser(auth), categoryId));
    }

    @GetMapping("/expert/consultation-requests")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT')")
    @Operation(summary = "Get expert's consultation requests")
    public ResponseEntity<Page<ConsultationRequestResponse>> getExpertRequests(
            Authentication auth,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(consultationService.getMentorRequests(getCurrentUser(auth), categoryId, teamId, status, priority, pageable));
    }

    @PutMapping("/expert/consultation-requests/{requestId}/accept")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT')")
    @Operation(summary = "Accept request")
    public ResponseEntity<ConsultationRequestResponse> acceptRequest(Authentication auth, @PathVariable UUID requestId) {
        return ResponseEntity.ok(consultationService.acceptRequest(getCurrentUser(auth), requestId));
    }

    @PutMapping("/expert/consultation-requests/{requestId}/reject")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT')")
    @Operation(summary = "Reject request")
    public ResponseEntity<ConsultationRequestResponse> rejectRequest(Authentication auth, @PathVariable UUID requestId, @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(consultationService.rejectRequest(getCurrentUser(auth), requestId, request.getReason()));
    }

    @PutMapping("/expert/consultation-requests/{requestId}/in-progress")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT')")
    @Operation(summary = "Mark request as in-progress")
    public ResponseEntity<ConsultationRequestResponse> markInProgress(Authentication auth, @PathVariable UUID requestId) {
        return ResponseEntity.ok(consultationService.markInProgress(getCurrentUser(auth), requestId));
    }

    @PutMapping("/expert/consultation-requests/{requestId}/resolve")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT')")
    @Operation(summary = "Resolve request")
    public ResponseEntity<ConsultationRequestResponse> resolveRequest(Authentication auth, @PathVariable UUID requestId) {
        return ResponseEntity.ok(consultationService.resolveRequest(getCurrentUser(auth), requestId));
    }

    // ==========================================
    // Team APIs
    // ==========================================

    @GetMapping("/teams/my-experts")
    @PreAuthorize("hasAnyRole('FPT_STUDENT', 'EXTERNAL_STUDENT')")
    @Operation(summary = "Get my assigned experts")
    public ResponseEntity<java.util.List<MentorProfileResponse>> getMyExperts(Authentication auth) {
        return ResponseEntity.ok(consultationService.getMyMentors(getCurrentUser(auth)));
    }

    @PostMapping("/consultation-requests")
    @PreAuthorize("hasAnyRole('FPT_STUDENT', 'EXTERNAL_STUDENT')")
    @Operation(summary = "Create consultation request")
    public ResponseEntity<ConsultationRequestResponse> createConsultationRequest(Authentication auth, @Valid @RequestBody CreateConsultationRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.createConsultationRequest(getCurrentUser(auth), request));
    }

    @GetMapping("/consultation-requests/my-team")
    @PreAuthorize("hasAnyRole('FPT_STUDENT', 'EXTERNAL_STUDENT')")
    @Operation(summary = "Get my team's requests")
    public ResponseEntity<Page<ConsultationRequestResponse>> getMyTeamRequests(
            Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(consultationService.getMyTeamRequests(getCurrentUser(auth), status, pageable));
    }

    @PutMapping("/consultation-requests/{requestId}/cancel")
    @PreAuthorize("hasAnyRole('FPT_STUDENT', 'EXTERNAL_STUDENT')")
    @Operation(summary = "Cancel request")
    public ResponseEntity<ConsultationRequestResponse> cancelRequest(Authentication auth, @PathVariable UUID requestId) {
        return ResponseEntity.ok(consultationService.cancelRequest(getCurrentUser(auth), requestId));
    }

    // ==========================================
    // Shared APIs (Expert & Team)
    // ==========================================

    @GetMapping("/consultation-requests/{requestId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT', 'FPT_STUDENT', 'EXTERNAL_STUDENT', 'ORGANIZER')")
    @Operation(summary = "Get request detail")
    public ResponseEntity<ConsultationRequestResponse> getConsultationRequestDetail(Authentication auth, @PathVariable UUID requestId) {
        return ResponseEntity.ok(consultationService.getConsultationRequestDetail(getCurrentUser(auth), requestId));
    }

    @GetMapping("/consultation-requests/{requestId}/messages")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT', 'FPT_STUDENT', 'EXTERNAL_STUDENT', 'ORGANIZER')")
    @Operation(summary = "Get request messages")
    public ResponseEntity<List<ConsultationMessageResponse>> getConsultationMessages(Authentication auth, @PathVariable UUID requestId) {
        return ResponseEntity.ok(consultationService.getConsultationMessages(getCurrentUser(auth), requestId));
    }

    @PostMapping("/consultation-requests/{requestId}/messages")
    @PreAuthorize("hasAnyRole('MENTOR', 'EXPERT', 'FPT_STUDENT', 'EXTERNAL_STUDENT')")
    @Operation(summary = "Send message in request")
    public ResponseEntity<ConsultationMessageResponse> sendMessage(Authentication auth, @PathVariable UUID requestId, @Valid @RequestBody MessageRequest messageDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.sendMessage(getCurrentUser(auth), requestId, messageDto));
    }
}
