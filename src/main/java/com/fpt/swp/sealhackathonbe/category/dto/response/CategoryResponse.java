package com.fpt.swp.sealhackathonbe.category.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private UUID categoryId;
    private UUID eventId;
    private String categoryName;
    private String description;
    private Integer sortOrder;
    private Boolean isActive;

    /**
     * Số đội hiện đăng ký trong category. Dùng làm THAM CHIẾU khi Organizer đặt
     * Advancement Top N cho round (round thuộc category, nên đội đi tiếp lấy từ đây chứ không
     * phải toàn event). Chỉ để cảnh báo mềm ở UI — không phải ràng buộc cứng, vì round thường
     * được tạo lúc chưa đội nào đăng ký. Có thể null ở các response không tra cứu số đội.
     */
    private Long teamCount;
}
