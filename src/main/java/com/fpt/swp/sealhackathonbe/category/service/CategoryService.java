package com.fpt.swp.sealhackathonbe.category.service;

import com.fpt.swp.sealhackathonbe.category.dto.request.CreateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface CategoryService {
    CategoryResponse create(CreateCategoryRequest request);
    List<CategoryResponse> getCategoriesByEventId(UUID eventId);
}
