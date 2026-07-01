package com.fpt.swp.sealhackathonbe.award.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class EventPrizeTotalRow {
    private UUID eventId;
    private String eventName;
    private String prizeCurrency;
    private BigDecimal totalPrize;
}
