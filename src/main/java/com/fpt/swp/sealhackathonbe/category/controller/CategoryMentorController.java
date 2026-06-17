package com.fpt.swp.sealhackathonbe.category.controller;

import com.fpt.swp.sealhackathonbe.category.dto.request.AssignMentorsRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryMentorResponse;
import com.fpt.swp.sealhackathonbe.category.service.CategoryMentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/category/mentor")
@RequiredArgsConstructor
public class CategoryMentorController {
    private final CategoryMentorService categoryMentorService;

    @PostMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_ORGANIZER')")
    public ResponseEntity<List<CategoryMentorResponse>> assignMentors(
            @PathVariable UUID categoryId,
            @Valid @RequestBody AssignMentorsRequest request
            ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryMentorService.assignMentors(categoryId, request));
    }
}
