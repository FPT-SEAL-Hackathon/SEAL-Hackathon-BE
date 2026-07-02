package com.fpt.swp.sealhackathonbe.category.service.impl;

import com.fpt.swp.sealhackathonbe.category.dto.request.AssignMentorsRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryMentorResponse;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import com.fpt.swp.sealhackathonbe.category.mapper.CategoryMapper;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryMentorRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.category.service.CategoryMentorService;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
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

    @Override
    public List<CategoryMentorResponse> assignMentors(UUID categoryId, AssignMentorsRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        List<User> mentors = userRepository.findAllById(request.getMentorIds());
        if (mentors.isEmpty()) {
            throw new IllegalArgumentException("No any mentors found");
        }
        List<CategoryMentor> categoryMentors = mentors
                .stream()
                .map(mentor -> CategoryMentor.builder()
                        .categoryMentorId(UUID.randomUUID())
                        .category(category)
                        .mentor(mentor)
                        .assignedAt(LocalDateTime.now())
                        .build()
                )
                .toList();

        categoryMentors = categoryMentorRepository.saveAll(categoryMentors);

        return categoryMentors.stream()
                .map(categoryMapper::categoryMentorResponse)
                .toList();
    }

    @Override
    public List<CategoryMentorResponse> getCategoryMentors(UUID categoryId) {
        List<CategoryMentor> categoryMentors = categoryMentorRepository.findByCategory_CategoryId(categoryId);
        return categoryMentors.stream()
                .map(categoryMapper::categoryMentorResponse)
                .toList();
    }
}
