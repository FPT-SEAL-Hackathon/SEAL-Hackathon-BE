package com.fpt.swp.sealhackathonbe.user.controller;

import com.fpt.swp.sealhackathonbe.submission.dto.PublicCountResponse;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Users", description = "Public APIs for user/judge statistics")
public class PublicUserController {

    private static final List<String> JUDGE_TYPE_NAMES = List.of("Internal Judge", "Guest Judge", "Expert");

    private final UserRepository userRepository;

    @Operation(summary = "Count all judges in the system")
    @GetMapping("/judges/count")
    public ResponseEntity<PublicCountResponse> countAllJudges() {
        Long count = userRepository.countActiveUsersByTypeNames(JUDGE_TYPE_NAMES);
        return ResponseEntity.ok(new PublicCountResponse(count));
    }
}
