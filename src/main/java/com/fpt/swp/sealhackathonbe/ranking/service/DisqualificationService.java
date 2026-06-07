package com.fpt.swp.sealhackathonbe.ranking.service;

import com.fpt.swp.sealhackathonbe.ranking.dto.DisqualificationDTO;
import com.fpt.swp.sealhackathonbe.ranking.dto.DisqualificationRequestDTO;

import java.util.List;
import java.util.UUID;

public interface DisqualificationService {
    DisqualificationDTO disqualify(DisqualificationRequestDTO requestDTO, UUID disqualifiedById);
    DisqualificationDTO reverseDisqualification(UUID disqualificationId, String reversalReason, UUID reversedById);
    List<DisqualificationDTO> getDisqualificationsByTeam(UUID teamId);
}
