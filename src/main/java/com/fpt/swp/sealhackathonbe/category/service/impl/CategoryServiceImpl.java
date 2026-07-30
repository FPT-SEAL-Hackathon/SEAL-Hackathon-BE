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
import com.fpt.swp.sealhackathonbe.team.repository.TeamsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;
    private final RoundRepository roundRepository;
    private final TeamsRepository teamsRepository;

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public List<CategoryResponse> getByEvent(UUID eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Gom số đội của mọi category trong event bằng 1 truy vấn rồi tra map, thay vì gọi
        // countByCategoryId cho từng category (N+1 — đắt với pool chỉ 10 kết nối).
        Map<UUID, Long> teamCounts = teamsRepository.countByCategoryGroupedForEvent(eventId)
                .stream()
                .filter(row -> row != null && row.length == 2 && row[0] != null)
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        return categoryRepository
                .findByEventEventIdAndIsActiveTrueOrderBySortOrderAsc(eventId)
                .stream()
                // Category chưa có đội nào không xuất hiện trong kết quả group by → mặc định 0.
                .map(c -> categoryMapper.toCategoryResponse(
                        c, teamCounts.getOrDefault(c.getCategoryId(), 0L)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        return categoryMapper.toCategoryResponse(category, teamsRepository.countByCategoryId(categoryId));
    }

    @Override
    @Transactional
    public CategoryResponse update(UUID categoryId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if (categoryRepository.existsByEventEventIdAndCategoryNameAndIsActiveTrueAndCategoryIdNot(
                category.getEvent().getEventId(), request.getCategoryName(), categoryId)) {
            throw new IllegalStateException("Category name already exists in this event");
        }
        if (request.getSortOrder() != null && categoryRepository.existsByEventEventIdAndSortOrderAndCategoryIdNot(
                category.getEvent().getEventId(), request.getSortOrder(), categoryId)) {
            throw new IllegalStateException("Sort order already exists");
        }

        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
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
