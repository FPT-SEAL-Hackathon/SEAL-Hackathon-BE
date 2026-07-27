package com.fpt.swp.sealhackathonbe.integration.repository.dto;

import lombok.Builder;
import lombok.Value;

/**
 * Du lieu "hoat dong phat trien" cua repo, lay best-effort qua cac API phu cua GitHub
 * (languages / contributors / commits). Tach rieng khoi RepositoryMetadata (core) vi:
 * - Core (/repos/{o}/{r}) la BAT BUOC; activity la TUY CHON (fail thi bo qua, khong chan submit).
 * - Giu fetchPublicMetadata (va test cua no) khong doi.
 * Moi field co the null neu call tuong ung that bai / bi rate-limit.
 */
@Value
@Builder
public class RepositoryActivity {
    String languagesJson;       // JSON {"Java": 12345, "TypeScript": 6789} (bytes theo GitHub)
    Integer contributorCount;   // so contributor
    String topContributorsJson; // JSON [{"login":"x","contributions":42,"avatarUrl":"..."}]
    Integer commitCount;        // tong so commit tren nhanh mac dinh (uoc luong qua Link header)
    String lastCommitSha;       // SHA commit moi nhat tren nhanh mac dinh
}
