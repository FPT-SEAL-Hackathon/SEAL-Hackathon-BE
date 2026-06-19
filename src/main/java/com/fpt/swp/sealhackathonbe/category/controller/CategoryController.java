package com.fpt.swp.sealhackathonbe.category.controller;

import com.fpt.swp.sealhackathonbe.category.dto.request.CreateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.request.UpdateCategoryRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryResponse;
import com.fpt.swp.sealhackathonbe.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/category/{eventId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<CategoryResponse> create(
            @PathVariable UUID eventId,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.create(eventId, request));
    }

    @GetMapping("/categories/{eventId}")
    public ResponseEntity<List<CategoryResponse>> getByEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(categoryService.getByEvent(eventId));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PutMapping("/category/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/category/{id}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }

}
