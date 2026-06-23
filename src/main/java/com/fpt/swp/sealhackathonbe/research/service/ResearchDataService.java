package com.fpt.swp.sealhackathonbe.research.service;

import com.fpt.swp.sealhackathonbe.research.dto.CalibrationSampleResponse;
import com.fpt.swp.sealhackathonbe.research.dto.CreateCalibrationSampleRequest;
import com.fpt.swp.sealhackathonbe.research.dto.DataExportLogResponse;
import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ResearchDataService {
    CalibrationSampleResponse createCalibrationSample(CreateCalibrationSampleRequest request, UUID currentUserId);

    List<CalibrationSampleResponse> getCalibrationSamplesByRound(UUID roundId);

    CalibrationSampleResponse getCalibrationSample(UUID sampleId);

    void deleteCalibrationSample(UUID sampleId);

    List<DataExportLogResponse> getExportLogs(UUID eventId);

    DownloadFileResponse exportResearchData(UUID eventId, UUID roundId, UUID categoryId, BigDecimal bucketSize, String type, UUID currentUserId);
}
