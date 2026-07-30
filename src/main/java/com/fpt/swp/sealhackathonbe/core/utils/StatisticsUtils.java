package com.fpt.swp.sealhackathonbe.core.utils;

import java.util.Collections;
import java.util.List;

public class StatisticsUtils {

    public static final String CONSENSUS_GOOD = "GOOD";
    public static final String CONSENSUS_WARNING = "WARNING";
    public static final String CONSENSUS_DANGER = "DANGER";

    /**
     * Calculates the median of a list of values.
     */
    public static double calculateMedian(List<Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        Collections.sort(scores);
        int size = scores.size();
        if (size % 2 == 0) {
            return (scores.get(size / 2 - 1) + scores.get(size / 2)) / 2.0;
        } else {
            return scores.get(size / 2);
        }
    }

    /**
     * Calculates the sample standard deviation (divide by n-1).
     */
    public static double calculateSampleStandardDeviation(List<Double> scores) {
        if (scores == null || scores.size() <= 1) {
            return 0.0; // Standard deviation is 0 for 1 or 0 elements
        }
        double sum = 0.0;
        for (double score : scores) {
            sum += score;
        }
        double mean = sum / scores.size();
        
        double varianceSum = 0.0;
        for (double score : scores) {
            varianceSum += Math.pow(score - mean, 2);
        }
        
        double variance = varianceSum / (scores.size() - 1);
        return Math.sqrt(variance);
    }

    /**
     * Evaluates the consensus status based on the standard deviation.
     *
     * @deprecated Nguong tuyet doi nay khong tinh den thang diem cua tieu chi: tieu chi cham
     * tren thang 100 thi SD > 1.5 xay ra gan nhu luon luon (=> luon DANGER), con thang 5 thi
     * gan nhu khong bao gio (=> luon GOOD). Dung
     * {@link #evaluateConsensusStatus(double, java.math.BigDecimal)} thay the.
     */
    @Deprecated
    public static String evaluateConsensusStatus(double sd) {
        if (sd > 1.5) {
            return CONSENSUS_DANGER;
        } else if (sd >= 0.5) {
            return CONSENSUS_WARNING;
        } else {
            return CONSENSUS_GOOD;
        }
    }

    // Nguong tinh theo TY LE tren thang diem toi da cua tieu chi, nen so sanh duoc giua cac
    // tieu chi khac thang. 15% cua thang la lech dang ke, 5% la con chap nhan duoc — quy doi
    // tuong duong nguong cu (1.5 va 0.5) tren thang 10, la thang pho bien nhat cua he thong.
    private static final double CONSENSUS_DANGER_RATIO = 0.15;
    private static final double CONSENSUS_WARNING_RATIO = 0.05;

    /**
     * Danh gia muc dong thuan theo do lech chuan CHUAN HOA theo thang diem cua tieu chi.
     * maxScore null hoac <= 0 -> quay ve nguong tuyet doi cu de khong vo du lieu cu.
     */
    public static String evaluateConsensusStatus(double sd, java.math.BigDecimal maxScore) {
        if (maxScore == null || maxScore.doubleValue() <= 0) {
            return evaluateConsensusStatus(sd);
        }
        double ratio = sd / maxScore.doubleValue();
        if (ratio > CONSENSUS_DANGER_RATIO) {
            return CONSENSUS_DANGER;
        } else if (ratio >= CONSENSUS_WARNING_RATIO) {
            return CONSENSUS_WARNING;
        } else {
            return CONSENSUS_GOOD;
        }
    }
}
