package com.fpt.swp.sealhackathonbe.appeal.service.impl;

import com.fpt.swp.sealhackathonbe.appeal.dto.AppealRequestDTO;
import com.fpt.swp.sealhackathonbe.appeal.dto.AppealResolutionDTO;
import com.fpt.swp.sealhackathonbe.appeal.dto.AppealResponseDTO;
import com.fpt.swp.sealhackathonbe.appeal.entity.AppealStatus;
import com.fpt.swp.sealhackathonbe.appeal.entity.Appeals;
import com.fpt.swp.sealhackathonbe.appeal.repository.AppealRepository;
import com.fpt.swp.sealhackathonbe.appeal.service.AppealService;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final AppealRepository appealRepository;
    private final TeamsRepository teamsRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AppealResponseDTO createAppeal(AppealRequestDTO request, UUID userId) {
        Teams team = teamsRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appeals appeal = Appeals.builder()
                .team(team)
                .event(event)
                .category(category)
                .title(request.getTitle())
                .reason(request.getReason())
                .status(AppealStatus.PENDING)
                .createdBy(user)
                .build();

        appeal = appealRepository.save(appeal);
        return mapToDTO(appeal);
    }

    @Override
    @Transactional
    public AppealResponseDTO resolveAppeal(UUID appealId, AppealResolutionDTO resolution, UUID adminUserId) {
        Appeals appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new RuntimeException("Appeal not found"));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        appeal.setStatus(resolution.getStatus());
        appeal.setResolutionNote(resolution.getResolutionNote());
        appeal.setResolvedBy(admin);

        appeal = appealRepository.save(appeal);
        return mapToDTO(appeal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppealResponseDTO> getAppealsByEvent(UUID eventId) {
        return appealRepository.findByEvent_EventIdOrderByCreatedAtDesc(eventId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppealResponseDTO> getAppealsByTeam(UUID teamId) {
        return appealRepository.findByTeam_TeamIdOrderByCreatedAtDesc(teamId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AppealResponseDTO mapToDTO(Appeals appeal) {
        return AppealResponseDTO.builder()
                .appealId(appeal.getAppealId())
                .teamId(appeal.getTeam().getTeamId())
                .teamName(appeal.getTeam().getTeamName())
                .eventId(appeal.getEvent().getEventId())
                .eventName(appeal.getEvent().getEventName())
                .categoryId(appeal.getCategory().getCategoryId())
                .categoryName(appeal.getCategory().getCategoryName())
                .title(appeal.getTitle())
                .reason(appeal.getReason())
                .status(appeal.getStatus())
                .resolutionNote(appeal.getResolutionNote())
                .resolvedBy(appeal.getResolvedBy() != null ? appeal.getResolvedBy().getUserId() : null)
                .resolvedByName(appeal.getResolvedBy() != null ? appeal.getResolvedBy().getFullName() : null)
                .createdBy(appeal.getCreatedBy().getUserId())
                .createdByName(appeal.getCreatedBy().getFullName())
                .createdAt(appeal.getCreatedAt())
                .updatedAt(appeal.getUpdatedAt())
                .build();
    }
}
