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
     */
    public static String evaluateConsensusStatus(double sd) {
        if (sd > 1.5) {
            return CONSENSUS_DANGER;
        } else if (sd >= 0.5) {
            return CONSENSUS_WARNING;
        } else {
            return CONSENSUS_GOOD;
        }
    }
}
