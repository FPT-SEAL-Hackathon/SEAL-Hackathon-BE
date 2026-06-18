package com.fpt.swp.sealhackathonbe.award.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class AwardPatternItemRequest {

    @NotNull(message = "Rank position must not be empty")
    @Min(value = 1, message = "Rank position must be at least 1")
    private Integer rankPosition;

    @NotNull(message = "Award tier ID must not be empty")
    private UUID awardTierId;

    @NotBlank(message = "Award title must not be empty")
    @Size(max = 300, message = "Award title must not exceed 300 characters")
    private String awardTitle;

    private String description;

    private BigDecimal prizeValue;

    @Size(max = 3, message = "Prize currency must not exceed 3 characters")
    private String prizeCurrency;
}
