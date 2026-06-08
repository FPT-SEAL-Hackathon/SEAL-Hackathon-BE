//package com.fpt.swp.sealhackathonbe.award.dto;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//@Getter
//@Setter
//public class AwardRequest {
//
//    @NotNull(message = "Event ID không được để trống")
//    private UUID eventId;
//
//    private UUID categoryId; // Có thể null nếu là giải toàn sự kiện
//
//    @NotNull(message = "Team ID không được để trống")
//    private UUID teamId;
//
//    @NotNull(message = "Award Tier ID không được để trống")
//    private UUID awardTierId;
//
//    @NotBlank(message = "Tên giải thưởng không được để trống")
//    @Size(max = 300, message = "Tên giải thưởng không được quá 300 ký tự")
//    private String awardTitle;
//
//    private String description;
//
//    private BigDecimal prizeValue;
//
//    @Size(max = 3, message = "Đơn vị tiền tệ tối đa 3 ký tự")
//    private String prizeCurrency; // Nếu trống, tầng Service sẽ tự gán mặc định là "VND"
//}