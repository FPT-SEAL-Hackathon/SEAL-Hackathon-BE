package com.fpt.swp.sealhackathonbe.category.controller;

import com.fpt.swp.sealhackathonbe.category.dto.request.CreateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/{eventId}")
    public CategoryResponse createCategory(@PathVariable UUID eventId,
                                           @Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.create(request);
    }
}
