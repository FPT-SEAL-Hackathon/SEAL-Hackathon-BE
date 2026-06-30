package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class AwardPrizeTotalResponse {
    private String prizeCurrency;
    private BigDecimal totalPrize;
}
