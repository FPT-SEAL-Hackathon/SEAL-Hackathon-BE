package com.fpt.swp.sealhackathonbe.category.controller;

import com.fpt.swp.sealhackathonbe.category.dto.request.CreateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.request.UpdateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/{eventId}")
    public List<CategoryResponse> getByEvent(@PathVariable UUID eventId) {
        return categoryService.getByEvent(eventId);
    }

    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable UUID id) {
        return categoryService.getById(id);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }

}
