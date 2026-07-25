package com.fpt.swp.sealhackathonbe.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả deactivate một user: tóm tắt tác động để Organizer nắm được hệ quả.
 * - transferredTeams: team đã tự chuyển quyền leader (dạng "TeamName → NewLeaderName").
 * - warnings: các cảnh báo cần xử lý tay (team đóng băng, user còn là judge/organizer...).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeactivateUserResult {

    @Builder.Default
    private List<String> transferredTeams = new ArrayList<>();

    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
