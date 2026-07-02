package com.fpt.swp.sealhackathonbe.consultation.service;

import com.fpt.swp.sealhackathonbe.consultation.dto.*;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ConsultationService {
    // Event Coordinator
    void assignMentorToCategory(UUID categoryId, UUID mentorId);
    void removeMentorFromCategory(UUID categoryId, UUID mentorId);
    List<MentorProfileResponse> getMentorsOfCategory(UUID categoryId);

    // Mentor
    List<AssignedCategoryResponse> getAssignedCategoriesForMentor(User mentor);
    // getTeamsForMentorCategory will reuse TeamResponse from team module if possible
    Page<ConsultationRequestResponse> getMentorRequests(User mentor, UUID categoryId, UUID teamId, String status, String priority, Pageable pageable);
    ConsultationRequestResponse acceptRequest(User mentor, UUID requestId);
    ConsultationRequestResponse rejectRequest(User mentor, UUID requestId, String reason);
    ConsultationRequestResponse markInProgress(User mentor, UUID requestId);
    ConsultationRequestResponse resolveRequest(User mentor, UUID requestId);

    // Team
    List<MentorProfileResponse> getMyMentors(User user);
    ConsultationRequestResponse createConsultationRequest(User user, CreateConsultationRequestRequest requestDto);
    Page<ConsultationRequestResponse> getMyTeamRequests(User user, String status, Pageable pageable);
    ConsultationRequestResponse cancelRequest(User user, UUID requestId);

    // Shared
    ConsultationRequestResponse getConsultationRequestDetail(User user, UUID requestId);
    List<ConsultationMessageResponse> getConsultationMessages(User user, UUID requestId);
    ConsultationMessageResponse sendMessage(User user, UUID requestId, MessageRequest messageDto);
}
