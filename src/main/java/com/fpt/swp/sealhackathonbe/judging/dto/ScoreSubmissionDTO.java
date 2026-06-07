package com.fpt.swp.sealhackathonbe.judging.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class ScoreSubmissionDTO {

    @NotNull(message = "Submission ID is required")
    private UUID submissionId;

    @NotNull(message = "Judge User ID is required")
    private UUID judgeUserId;

    @NotNull(message = "Event Criterion ID is required")
    private UUID eventCriterionId;

    @NotNull(message = "Score value is required")
    @DecimalMin(value = "0.0", message = "Score must be greater than or equal to 0")
    private BigDecimal scoreValue;

    private String comment;

    private Boolean isCalibration;

    @NotNull(message = "Actor ID (the person performing this action) is required")
    private UUID actorId;

    @NotBlank(message = "Reason for this change is mandatory")
    private String reason;

    public ScoreSubmissionDTO() {
    }

    public ScoreSubmissionDTO(UUID submissionId, UUID judgeUserId, UUID eventCriterionId, BigDecimal scoreValue,
                              String comment, Boolean isCalibration, UUID actorId, String reason) {
        this.submissionId = submissionId;
        this.judgeUserId = judgeUserId;
        this.eventCriterionId = eventCriterionId;
        this.scoreValue = scoreValue;
        this.comment = comment;
        this.isCalibration = isCalibration;
        this.actorId = actorId;
        this.reason = reason;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID submissionId) {
        this.submissionId = submissionId;
    }

    public UUID getJudgeUserId() {
        return judgeUserId;
    }

    public void setJudgeUserId(UUID judgeUserId) {
        this.judgeUserId = judgeUserId;
    }

    public UUID getEventCriterionId() {
        return eventCriterionId;
    }

    public void setEventCriterionId(UUID eventCriterionId) {
        this.eventCriterionId = eventCriterionId;
    }

    public BigDecimal getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(BigDecimal scoreValue) {
        this.scoreValue = scoreValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getIsCalibration() {
        return isCalibration;
    }

    public void setIsCalibration(Boolean isCalibration) {
        this.isCalibration = isCalibration;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
