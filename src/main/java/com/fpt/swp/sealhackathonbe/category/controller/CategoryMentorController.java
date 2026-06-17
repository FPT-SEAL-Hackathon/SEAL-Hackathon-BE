package com.fpt.swp.sealhackathonbe.category.controller;

import com.fpt.swp.sealhackathonbe.category.dto.request.AssignMentorsRequest;
import com.fpt.swp.sealhackathonbe.category.dto.response.CategoryMentorResponse;
import com.fpt.swp.sealhackathonbe.category.service.CategoryMentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/categories/mentor")
@RequiredArgsConstructor
public class CategoryMentorController {
    private final CategoryMentorService categoryMentorService;

    @PostMapping("/{categoryId}")
    public List<CategoryMentorResponse> assignMentors(
            @PathVariable UUID categoryId,
            @Valid @RequestBody AssignMentorsRequest request
            ) {
        return categoryMentorService.assignMentors(categoryId, request);
    }
}
