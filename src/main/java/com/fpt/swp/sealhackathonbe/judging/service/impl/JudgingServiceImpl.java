package com.fpt.swp.sealhackathonbe.judging.service.impl;

import com.fpt.swp.sealhackathonbe.criteria.repository.EventCriterionRepository;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.entity.*;
import com.fpt.swp.sealhackathonbe.judging.repository.*;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import com.fpt.swp.sealhackathonbe.round.entity.RoundCriteria;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.repository.RoundCriterionRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JudgingServiceImpl implements JudgingService {

    private final JudgingRepository judgingRepository;
    private final UserRepository userRepository;
    private final SubmissionsRepository submissionRepository;
    //    private final EvaluationAuditLogRepository evaluationAuditLogRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final RoundCriterionRepository roundCriterionRepository;

    @Autowired
    public JudgingServiceImpl(JudgingRepository judgingRepository,
                              UserRepository userRepository,
                              SubmissionsRepository submissionRepository,
                              EventCriterionRepository eventCriterionRepository,
//                              EvaluationAuditLogRepository evaluationAuditLogRepository,
                              RoundJudgeRepository roundJudgeRepository,
                              RoundCriterionRepository roundCriterionRepository) {
        this.judgingRepository = judgingRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
//        this.evaluationAuditLogRepository = evaluationAuditLogRepository;
        this.roundJudgeRepository = roundJudgeRepository;
        this.roundCriterionRepository = roundCriterionRepository;
    }

    @Override
    @Transactional
    public void recordJudging(ScoreSubmissionDTO dto) {

        // 1. Fetch & validate that the submission exists
        Submissions submission = submissionRepository.findById(dto.getSubmissionId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with ID: " + dto.getSubmissionId()));

        // 2. Fetch & validate that the judge exists
        RoundJudge judge = roundJudgeRepository.findById(dto.getRoundJudgeId())
                .orElseThrow(() -> new EntityNotFoundException("Judge User not found with ID: " + dto.getRoundJudgeId()));

        // 3. Fetch & validate that the actor (audit user) exists
        User actor = userRepository.findById(dto.getActorId())
                .orElseThrow(() -> new EntityNotFoundException("Actor User not found with ID: " + dto.getActorId()));

        // 4. Fetch & validate that the event criterion exists
        RoundCriteria criterion = roundCriterionRepository.findById(dto.getRoundCriterionId())
                .orElseThrow(() -> new EntityNotFoundException("Round Criterion not found with ID: " + dto.getRoundCriterionId()));

        // 5. Validate that the score value does not exceed the maximum allowed value
        if (dto.getScoreValue().compareTo(criterion.getEventCriterion().getMaxScore()) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Score value %s exceeds the maximum allowed value %s for criterion '%s'.",
                    dto.getScoreValue(), criterion.getEventCriterion().getMaxScore(), criterion.getEventCriterion().getCriterionName()
            ));
        }

        // 6. Get Criterion weight
        BigDecimal weight = criterion.getWeight() != null ? criterion.getWeight() : BigDecimal.ONE;
        BigDecimal weightedScore = dto.getScoreValue().multiply(weight);

        // 7. Check if a score already exists for this submission, judge, and criterion
        Optional<Judging> existingScoreOpt = judgingRepository
                .findBySubmissionIdAndRoundJudgeIdAndRoundCriterionId(
                        dto.getSubmissionId(), dto.getRoundJudgeId(), dto.getRoundCriterionId()
                );

        String actionType;
        String oldValue = null;
        String newValue;
        Judging savedJudging;

        String formattedNewComment = dto.getComment() != null ? dto.getComment().replace("\"", "\\\"") : "";
        newValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", weightedScore, formattedNewComment);

        if (existingScoreOpt.isPresent()) {
            Judging existingJudging = existingScoreOpt.get();
            actionType = "SCORE_UPDATE";
            String formattedOldComment = existingJudging.getComment() != null ? existingJudging.getComment().replace("\"", "\\\"") : "";
            oldValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", existingJudging.getScoreValue(), formattedOldComment);

            existingJudging.setScoreValue(weightedScore);
            existingJudging.setComment(dto.getComment());
            if (dto.getIsCalibration() != null) {
                existingJudging.setIsCalibration(dto.getIsCalibration());
            }
            savedJudging = judgingRepository.save(existingJudging);
        } else {
            actionType = "SCORE_CREATE";
            Judging newJudging = new Judging();
            newJudging.setSubmission(submission);
            newJudging.setRoundJudge(judge);
            newJudging.setRoundCriterion(criterion);
            newJudging.setScoreValue(weightedScore);
            newJudging.setComment(dto.getComment());
            newJudging.setIsCalibration(dto.getIsCalibration() != null ? dto.getIsCalibration() : false);
            savedJudging = judgingRepository.save(newJudging);
        }

        // 8. Extract Team and Event from the submission hierarchy
        Teams team = submission.getTeam();
        Event event = (team != null) ? team.getEvent() : null;

        if (event == null) {
            throw new IllegalStateException("Could not log evaluation audit because the submission's event context is missing.");
        }

//        // 9. Create and save the EvaluationAuditLog
//        EvaluationAuditLog auditLog = new EvaluationAuditLog();
//        auditLog.setEvent(event);
//        auditLog.setActionType(actionType);
//        auditLog.setActor(actor);
//        auditLog.setScore(savedJudging);
//        auditLog.setTeam(team);
//        auditLog.setSubmission(submission);
//        auditLog.setOldValue(oldValue);
//        auditLog.setNewValue(newValue);
//        auditLog.setReason(dto.getReason());
//
//        evaluationAuditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgingDTO> getScoresBySubmission(UUID submissionId) {
        return judgingRepository.findBySubmissionId(submissionId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JudgingDTO> getScoresByJudgeId(UUID roundJudgeId) {
        return judgingRepository.findByRoundJudgeId(roundJudgeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private JudgingDTO convertToDTO(Judging judging) {
        return JudgingDTO.builder()
                .id(judging.getId())
                .submissionId(judging.getSubmission() != null ? judging.getSubmission().getSubmissionId() : null)
                .roundJudgeId(judging.getRoundJudge() != null ? judging.getRoundJudge().getUserId() : null)
                .judgeName(judging.getRoundJudge() != null ? judging.getRoundJudge().getUserId() : null)
                .roundCriterionId(judging.getRoundCriterion() != null ? judging.getRoundCriterion().getRoundCriterionId() : null)
                .criterionName(judging.getRoundCriterion() != null ? judging.getRoundCriterion().getCriterionName() : null)
                .scoreValue(judging.getScoreValue())
                .comment(judging.getComment())
                .scoredAt(judging.getScoredAt())
                .updatedAt(judging.getUpdatedAt())
                .isCalibration(judging.getIsCalibration())
                .build();
    }


}
