//package com.fpt.swp.sealhackathonbe.award.service;
//
//import com.fpt.swp.sealhackathonbe.award.dto.AwardRequest;
//import com.fpt.swp.sealhackathonbe.award.dto.AwardResponse;
//
//import java.util.List;
//import java.util.UUID;
//
//public interface AwardService {
//    // Hàm xử lý trao giải, cần truyền thêm ID của Admin đang đăng nhập thực hiện thao tác
//    AwardResponse grantAward(AwardRequest request, UUID adminId);
//
//    // Hàm lấy chi tiết 1 giải thưởng
//    AwardResponse getAwardById(UUID awardId);
//
//    // Hàm lấy danh sách giải thưởng của một sự kiện (phục vụ hiển thị hoặc kiểm tra)
//    List<AwardResponse> getAwardsByEvent(UUID eventId);
//}