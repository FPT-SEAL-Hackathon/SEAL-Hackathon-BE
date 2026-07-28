package com.fpt.swp.sealhackathonbe.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả xử lý team khi một user bị deactivate:
 * - transfers: các team user đang làm leader đã được chuyển quyền cho thành viên khác.
 * - frozenTeams: các team user là leader nhưng KHÔNG còn thành viên active khác
 *   (giữ nguyên, không giải tán vì deactivate là hành động đảo ngược được) → cần cảnh báo.
 * Membership của user LUÔN được giữ (không giảm sĩ số team).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeadershipReassignmentResult {

    @Builder.Default
    private List<TransferInfo> transfers = new ArrayList<>();

    @Builder.Default
    private List<String> frozenTeams = new ArrayList<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransferInfo {
        private String teamName;
        private String newLeaderName;
    }
}
