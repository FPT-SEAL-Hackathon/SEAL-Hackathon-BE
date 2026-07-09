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
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryMentorServiceImpl implements CategoryMentorService {
    private final CategoryRepository categoryRepository;
    private final CategoryMentorRepository categoryMentorRepository;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;

    @Override
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

        for (User mentor : mentors) {
            String typeName = mentor.getUserType().getTypeName();
            if (typeName.toLowerCase().contains("judge")) {
                mentor.setUserType(expertType);
                userRepository.save(mentor);
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

        return categoryMentors.stream()
                .map(categoryMapper::toCategoryMentorResponse)
                .toList();
    }

    public List<UserResponse> getAllMentors() {
        return userRepository.findAll()
                .stream()
                .filter(user -> {
                    String type = user.getUserType().getTypeName();
                    return type.equalsIgnoreCase("Mentor") || type.equalsIgnoreCase("Expert");
                })
                .map(user -> UserResponse.builder()
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .toList();
    }

    @Override
    public List<CategoryMentorResponse> getMentorsByCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found");
        }
        ;
        return categoryMentorRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(categoryMapper::toCategoryMentorResponse)
                .toList();
    }

    @Override
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
