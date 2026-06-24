package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionResponse;

import java.util.List;
import java.util.UUID;

public interface SubmissionQueryService {
    // Contract chi phuc vu query, duoc cac endpoint GET trong SubmissionController su dung.
    SubmissionResponse getSubmissionById(UUID submissionId);

    // TeamID + RoundID la khoa duy nhat nghiep vu cua mot submission.
    SubmissionResponse getSubmissionByTeamAndRound(UUID teamId, UUID roundId, UUID currentUserId);

    // Lay tat ca bai nop trong mot round cho man hinh review/cham diem.
    List<SubmissionResponse> getSubmissionsByRound(UUID roundId);

    // Lay cac bai nop trong round chua duoc review/cham diem.
    List<SubmissionResponse> getUnreviewSubmissionByRound(UUID roundId);

    // Lay bai nop cua cac team thuoc event; submission khong chua EventID truc tiep.
    List<SubmissionResponse> findByEventId(UUID eventId);
}
