package com.fpt.swp.sealhackathonbe.category.service.impl;

import com.fpt.swp.sealhackathonbe.category.dto.response.MentorDashboardSummaryResponse;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryMentorRepository;
import com.fpt.swp.sealhackathonbe.category.service.MentorDashboardService;
import com.fpt.swp.sealhackathonbe.team.service.TeamService;
import com.fpt.swp.sealhackathonbe.team.dto.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Tổng hợp dữ liệu cho Mentor Dashboard trong 1 request.
 *
 * Logic:
 * 1. Dùng categoryMentorRepository.findByMentor_UserId() để lấy tất cả category được assign (đã có sẵn)
 * 2. Từ CategoryMentor → lấy Category → lấy Event (lazy-load)
 * 3. Tập hợp distinct eventIds → gọi teamService.getByEventId() per event
 * 4. Build response
 *
 * Thay thế waterfall: 1 events → N categories/event → M mentor-check/category = 50+ HTTP calls
 * Thành: 1 query DB + N team queries (N = số event distinct, thường 1-3)
 */
@Service
@RequiredArgsConstructor
public class MentorDashboardServiceImpl implements MentorDashboardService {

    private final CategoryMentorRepository categoryMentorRepository;
    private final TeamService teamService;

    @Override
    @Transactional(readOnly = true)
    public MentorDashboardSummaryResponse getDashboardSummary(UUID mentorId) {
        // 1. Lấy toàn bộ CategoryMentor record của mentor này — 1 query duy nhất
        List<CategoryMentor> assignments = categoryMentorRepository.findByMentor_UserId(mentorId);

        if (assignments.isEmpty()) {
            return MentorDashboardSummaryResponse.builder()
                    .assignedCategories(Collections.emptyList())
                    .teams(Collections.emptyList())
                    .build();
        }

        // 2. Lấy distinct eventIds từ các category
        Set<UUID> eventIds = assignments.stream()
                .map(cm -> cm.getCategory().getEvent().getEventId())
                .collect(Collectors.toSet());

        // 3. Load teams cho mỗi event (thường 1-3 event, không phải 50+)
        List<TeamResponse> allTeams = eventIds.stream()
                .flatMap(eid -> {
                    try {
                        return teamService.getByEventId(eid).stream();
                    } catch (Exception e) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .toList();

        // 4. Build team count per category
        Map<UUID, Long> teamCountByCategory = allTeams.stream()
                .filter(t -> t.getCategoryId() != null)
                .collect(Collectors.groupingBy(
                        TeamResponse::getCategoryId,
                        Collectors.counting()
                ));

        // 5. Build AssignedCategoryDto list
        List<MentorDashboardSummaryResponse.AssignedCategoryDto> assignedCategories = assignments.stream()
                .map(cm -> {
                    Category cat = cm.getCategory();
                    return MentorDashboardSummaryResponse.AssignedCategoryDto.builder()
                            .categoryId(cat.getCategoryId().toString())
                            .categoryName(cat.getCategoryName())
                            .description(cat.getDescription())
                            .eventId(cat.getEvent().getEventId().toString())
                            .eventName(cat.getEvent().getEventName())
                            .teamCount(teamCountByCategory.getOrDefault(cat.getCategoryId(), 0L).intValue())
                            .isActive(Boolean.TRUE.equals(cat.getIsActive()))
                            .build();
                })
                .toList();

        return MentorDashboardSummaryResponse.builder()
                .assignedCategories(assignedCategories)
                .teams(allTeams)
                .build();
    }
}
