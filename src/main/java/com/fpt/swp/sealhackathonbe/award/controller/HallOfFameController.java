//package com.fpt.swp.sealhackathonbe.award.controller;
//
//import com.fpt.swp.sealhackathonbe.award.dto.HallOfFameResponse;
//import com.fpt.swp.sealhackathonbe.award.service.AwardService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.TokenHashUtil.List;
//
//@RestController
//@RequestMapping("/api/v1/public/hall-of-fame") // Đường dẫn chứa chữ "public"
//@RequiredArgsConstructor
//public class HallOfFameController {
//
//    private final AwardService awardService;
//
//    /**
//     * API xem Sảnh Danh Vọng (Không cần đăng nhập)
//     * GET /api/v1/public/hall-of-fame
//     */
//    @GetMapping
//    public ResponseEntity<List<HallOfFameResponse>> getHallOfFame() {
//        List<HallOfFameResponse> data = awardService.getHallOfFameData();
//        return ResponseEntity.ok(data);
//    }
//}