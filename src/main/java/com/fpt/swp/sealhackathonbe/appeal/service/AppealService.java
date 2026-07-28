package com.fpt.swp.sealhackathonbe.appeal.service;

import com.fpt.swp.sealhackathonbe.appeal.dto.AppealRequestDTO;
import com.fpt.swp.sealhackathonbe.appeal.dto.AppealResolutionDTO;
import com.fpt.swp.sealhackathonbe.appeal.dto.AppealResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AppealService {
    AppealResponseDTO createAppeal(AppealRequestDTO request, UUID userId);
    AppealResponseDTO resolveAppeal(UUID appealId, AppealResolutionDTO resolution, UUID adminUserId);
    List<AppealResponseDTO> getAppealsByEvent(UUID eventId);
    List<AppealResponseDTO> getAppealsByTeam(UUID teamId);
}
