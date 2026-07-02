package com.fpt.swp.sealhackathonbe.publicapi.controller;

import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping({
            "/api/v1/categories/categories/{eventId}",
            "/api/v1/public/categories/events/{eventId}"
    })
    public ResponseEntity<List<CategoryResponse>> getByEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(categoryService.getByEvent(eventId));
    }
}
