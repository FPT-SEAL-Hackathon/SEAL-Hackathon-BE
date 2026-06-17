package com.fpt.swp.sealhackathonbe.category.service.impl;

import com.fpt.swp.sealhackathonbe.category.dto.request.CreateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.request.UpdateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.mapper.CategoryMapper;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.category.service.CategoryService;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private final RoundRepository roundRepository;

    @Override
    public CategoryResponse create(UUID eventId, CreateCategoryRequest request) {
        Event event = eventRepository.findByEventIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        if (categoryRepository.existsByEventEventIdAndCategoryNameAndIsActiveTrue(event.getEventId(), request.getCategoryName())) {
            throw new IllegalStateException("Category name already exists in this event");
        }
        Integer sortOrder = request.getSortOrder();
        if (sortOrder == null) {
            sortOrder = categoryRepository.findMaxSortOrderByEventEventId(eventId) + 1;
        }
        if (categoryRepository.existsByEventEventIdAndSortOrder(eventId, sortOrder)) {
            throw new IllegalStateException("Sort order already exists");
        }
        Category category = Category.builder()
                .event(event)
                .categoryId(UUID.randomUUID())
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .sortOrder(sortOrder)
                .isActive(true)
                .build();
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getByEvent(UUID eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        return categoryRepository
                .findByEventEventIdAndIsActiveTrueOrderBySortOrderAsc(eventId)
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public CategoryResponse getById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse update(UUID categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if (categoryRepository.existsByEventEventIdAndCategoryNameAndIsActiveTrue(category.getEvent().getEventId(), request.getCategoryName())) {
            throw new IllegalStateException("Category name already exists in this event");
        }
        if (categoryRepository.existsByEventEventIdAndSortOrder(category.getEvent().getEventId(), request.getSortOrder())) {
            throw new IllegalStateException("Sort order already exists");
        }

        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        if (roundRepository.existsByCategoryCategoryId(categoryId)) {
            throw new IllegalStateException("Cannot delete category because it has rounds");
        }
        category.setIsActive(false);
        categoryRepository.save(category);
    }

}
