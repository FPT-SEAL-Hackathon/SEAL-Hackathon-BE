package com.fpt.swp.sealhackathonbe.category.service;

import com.fpt.swp.sealhackathonbe.category.dto.request.CreateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.request.UpdateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CategoryService {
    CategoryResponse create(UUID eventId,CreateCategoryRequest request);
    List<CategoryResponse> getByEvent(UUID eventId);
    CategoryResponse getById(UUID categoryId);
    CategoryResponse update(UUID categoryId, UpdateCategoryRequest request);
    void delete(UUID categoryId);
}
