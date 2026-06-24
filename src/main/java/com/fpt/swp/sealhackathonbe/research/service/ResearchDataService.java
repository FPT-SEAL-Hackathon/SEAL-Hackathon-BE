package com.fpt.swp.sealhackathonbe.research.service;

import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface ResearchDataService {
    DownloadFileResponse exportResearchData(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize, String type, UUID currentUserId);
}
