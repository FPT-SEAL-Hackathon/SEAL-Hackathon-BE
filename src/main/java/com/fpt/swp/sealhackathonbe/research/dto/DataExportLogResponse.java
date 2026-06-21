package com.fpt.swp.sealhackathonbe.research.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DataExportLogResponse(
        UUID exportId,
        UUID eventId,
        String eventName,
        UUID exportedById,
        String exportedByName,
        LocalDateTime exportedAt,
        String fileFormat,
        Integer rowCount,
        String notes
) {
}
