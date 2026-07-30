package com.fpt.swp.sealhackathonbe.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateItemResponse {
    private String id;
    private String type; // "PARTICIPATION" or "AWARD"
    private UUID awardId;
    private UUID eventId;
    private String eventName;
    private UUID categoryId;
    private String categoryName;
    private UUID teamId;
    private String teamName;
    private String title;
    private String awardTierName;
    private Instant publishedAt;
}
