package com.fpt.swp.sealhackathonbe.research.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mot dong = mot (bai mau x tieu chi).
 *
 * submissionId/sampleLabel duoc them vao vi truoc day ket qua chi gom theo TIEU CHI: khi round
 * hieu chuan co nhieu hon mot bai mau, diem cua cac bai bi tron vao nhau khi tinh median/SD con
 * judgeScores thi bi ghi de chi con bai cuoi cung. Tach theo bai nop moi so sanh dung duoc.
 *
 * maxPossibleScore cho phep chuan hoa theo thang diem cua tieu chi (SD tuyet doi khong so sanh
 * duoc giua tieu chi thang 5 va thang 100).
 *
 * scoreDistribution la danh sach diem cua TAT CA giam khao (khong kem danh tinh) de ve phan bo;
 * judgeScores van giu de nguoi dung tra ve diem cua chinh minh.
 */
public record ConsensusMatrixResponse(
        UUID submissionId,
        String sampleLabel,
        String criteriaName,
        BigDecimal median,
        BigDecimal minScore,
        BigDecimal maxScore,
        BigDecimal standardDeviation,
        BigDecimal maxPossibleScore,
        String status,
        List<BigDecimal> scoreDistribution,
        Map<String, BigDecimal> judgeScores // Map of Judge UserID to their score for this criteria
) {
}
