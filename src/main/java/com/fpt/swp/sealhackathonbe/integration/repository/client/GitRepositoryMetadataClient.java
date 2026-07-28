package com.fpt.swp.sealhackathonbe.integration.repository.client;

import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryActivity;
import com.fpt.swp.sealhackathonbe.integration.repository.dto.RepositoryMetadata;
import com.fpt.swp.sealhackathonbe.integration.repository.entity.RepositoryProvider;

public interface GitRepositoryMetadataClient {
    RepositoryProvider getProvider();
    boolean supports(String repositoryUrl);
    RepositoryMetadata fetchPublicMetadata(String repositoryUrl);

    /**
     * Lay du lieu hoat dong (languages/contributors/commits) — BEST-EFFORT.
     * KHONG bao gio nem exception: loi tung phan tra ve field null.
     * Mac dinh tra ve activity rong de caller khong can null-check client.
     */
    default RepositoryActivity fetchActivity(String repositoryUrl) {
        return RepositoryActivity.builder().build();
    }

    /**
     * Lay README (raw markdown) — BEST-EFFORT, lazy (goi khi nguoi dung mo). Tra null neu
     * khong co README / loi / rate-limit. KHONG nem exception.
     */
    default String fetchReadme(String repositoryUrl) {
        return null;
    }
}
