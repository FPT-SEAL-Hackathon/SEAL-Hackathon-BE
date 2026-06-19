package com.fpt.swp.sealhackathonbe.studentdownload.service;

import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;

import java.util.UUID;

public interface StudentDownloadService {
    DownloadFileResponse downloadRoundProblem(UUID roundId, UUID currentUserId, String type);

    DownloadFileResponse downloadRoundProblemCsv(UUID roundId, UUID currentUserId);

    DownloadFileResponse downloadRoundProblemZip(UUID roundId, UUID currentUserId);
}
