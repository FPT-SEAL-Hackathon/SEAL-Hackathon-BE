package com.fpt.swp.sealhackathonbe.judging.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tinh trang hoan thanh vong hieu chuan cua MOT giam khao.
 *
 * Truoc day khong co man hinh nao cho Organizer biet giam khao nao chua cham bai mau: he thong
 * khong nhac, khong chan, va giam khao vang mat cung khong xuat hien o bat ky bao cao nao
 * (khong co diem thi khong co dong du lieu). Ket qua la ca hoi dong tuong da hieu chuan xong
 * trong khi thuc te median duoc tinh tren mot vai nguoi.
 */
@Getter
@Builder
public class CalibrationJudgeStatusResponse {
    private UUID judgeUserId;
    private String judgeName;
    private String email;

    /** Tong so bai mau cua vong hieu chuan. */
    private int sampleCount;

    /** So bai mau ma giam khao nay da cham DU moi tieu chi. */
    private int completedSampleCount;

    /** Tong so o diem da cham (bai mau x tieu chi), de thay tien do khi cham do dang. */
    private int scoredCriterionCount;

    /** So o diem can cham = sampleCount x so tieu chi cua vong. */
    private int expectedCriterionCount;

    /** true khi da cham du moi tieu chi cua moi bai mau. */
    private boolean completed;

    private LocalDateTime lastScoredAt;
}
