package com.fpt.swp.sealhackathonbe.integration.repository.client;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;

public interface GitRepositoryMetadataClient {
    RepositoryProvider getProvider();
    boolean supports(String repositoryUrl);
    RepositoryMetadata fetchPublicMetadata(String repositoryUrl);
}
