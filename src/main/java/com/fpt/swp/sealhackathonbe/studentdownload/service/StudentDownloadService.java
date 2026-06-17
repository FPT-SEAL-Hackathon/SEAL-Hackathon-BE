package com.fpt.swp.sealhackathonbe.studentdownload.service;

import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;

import java.util.UUID;

public interface StudentDownloadService {
    DownloadFileResponse downloadRoundProblemCsv(UUID roundId, UUID currentUserId);
}
