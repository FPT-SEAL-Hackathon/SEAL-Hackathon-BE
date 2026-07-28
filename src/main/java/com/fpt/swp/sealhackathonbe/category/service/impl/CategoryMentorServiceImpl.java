package com.fpt.swp.sealhackathonbe.category.service.impl;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.category.dto.request.AssignMentorsRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryMentorResponse;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import com.fpt.swp.sealhackathonbe.category.mapper.CategoryMapper;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryMentorRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.category.service.CategoryMentorService;
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class CategoryMentorServiceImpl implements CategoryMentorService {
    private final CategoryRepository categoryRepository;
    private final CategoryMentorRepository categoryMentorRepository;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final NotificationService notificationService;
    private final RoundJudgeRepository roundJudgeRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public List<CategoryMentorResponse> assignMentors(UUID categoryId, AssignMentorsRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        List<User> mentors = userRepository.findAllById(request.getExpertIds());
        if (mentors.isEmpty()) {
            throw new IllegalArgumentException("No any mentors found");
        }
        List<UUID> existingMentorIds = categoryMentorRepository.findMentorIdsByCategoryId(categoryId);

        UserType expertType = userTypeRepository.findByTypeName("Expert")
                .orElseThrow(() -> new RuntimeException("Expert role not found"));

        // BR-19 (chiều ngược): nếu user đã là judge active trong bất kỳ round nào
        // của category này → không được assign làm mentor cho cùng category đó.
        for (User mentor : mentors) {
            boolean isJudgeInCategory = roundJudgeRepository
                    .existsActiveJudgeInCategory(mentor.getUserId(), categoryId);
            if (isJudgeInCategory) {
                throw new IllegalArgumentException(
                        "User " + mentor.getFullName() + " is already a judge in a round of this category");
            }
        }

        List<CategoryMentor> categoryMentors = mentors
                .stream()
                .filter(mentor -> !existingMentorIds.contains(mentor.getUserId()))
                .map(mentor -> CategoryMentor.builder()
                        .category(category)
                        .mentor(mentor)
                        .assignedAt(LocalDateTime.now())
                        .build())
                .toList();

        if (!categoryMentors.isEmpty()) {
            categoryMentors = categoryMentorRepository.saveAll(categoryMentors);
        }

        // Get current authenticated user (admin who is assigning)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String adminEmail = authentication.getName();
        User admin = userRepository.findByEmail(adminEmail);

        // Send notification to each newly assigned mentor
        com.fpt.swp.sealhackathonbe.event.entity.Event event = category.getEvent();
        for (CategoryMentor cm : categoryMentors) {
            try {
                String title = "New Mentor Assignment";
                String body = String.format(
                        "You have been assigned as a Mentor for Category: %s, Event: %s.\n" +
                        "Event Date: %s to %s\n" +
                        "Event Link: %s/events/%s",
                        category.getCategoryName(),
                        event.getEventName(),
                        event.getEventStartDate(),
                        event.getEventEndDate(),
                        frontendUrl,
                        event.getEventId());
                notificationService.sendNotification(
                        cm.getMentor().getUserId(),
                        admin != null ? admin.getUserId() : cm.getMentor().getUserId(),
                        event.getEventId(),
                        title,
                        body);
            } catch (Exception e) {
                log.warn("Failed to send mentor notification", e);
            }
        }

        return categoryMentors.stream()
                .map(categoryMapper::toCategoryMentorResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllMentors() {
        return userRepository.findExpertsMentorsJudges()
                .stream()
                .map(user -> UserResponse.builder()
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(toApiName(getRoleName(user)))
                        .roleName(getRoleName(user))
                        .build())
                .toList();
    }

    private String getRoleName(User user) {
        return user.getUserType() != null ? user.getUserType().getTypeName() : null;
    }

    private String toApiName(String value) {
        return value == null ? null : value.trim().replace(' ', '_').toUpperCase();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryMentorResponse> getMentorsByCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found");
        }
        return categoryMentorRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(categoryMapper::toCategoryMentorResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryMentorResponse> getCategoryMentors(UUID categoryId) {
        List<CategoryMentor> categoryMentors = categoryMentorRepository.findByCategory_CategoryId(categoryId);
        return categoryMentors.stream()
                .map(categoryMapper::toCategoryMentorResponse)
                .toList();
    }

    @Override
    public void removeMentor(UUID categoryId, UUID mentorId) {
        CategoryMentor cm = categoryMentorRepository
                .findByCategory_CategoryIdAndMentor_UserId(categoryId, mentorId)
                .orElseThrow(() -> new EntityNotFoundException("Mentor is not assigned to this category"));
        categoryMentorRepository.delete(cm);
    }
}
