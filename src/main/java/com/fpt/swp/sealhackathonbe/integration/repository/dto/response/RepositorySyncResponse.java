package com.fpt.swp.sealhackathonbe.integration.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositorySyncResponse {
    private UUID repositoryId;
    private String status;
    private int itemsFetched;
    private int itemsCreated;
    private int itemsUpdated;
    private int itemsFailed;
    private boolean hasMore;
    private String message;
}
