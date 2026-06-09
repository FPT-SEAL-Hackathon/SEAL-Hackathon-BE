package com.fpt.swp.sealhackathonbe.category.service.impl;

import com.fpt.swp.sealhackathonbe.category.dto.request.CreateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.mapper.CategoryMapper;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.category.service.CategoryService;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Category category = Category.builder()
                .event(event)
                .categoryId(UUID.randomUUID())
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .isActive(true)
                .build();
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getCategoriesByEventId(UUID eventId) {
        return null;
    }

}
