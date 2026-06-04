package com.fpt.swp.sealhackathonbe.judging.service.impl;

import com.fpt.swp.sealhackathonbe.judging.dto.JudgingDTO;
import com.fpt.swp.sealhackathonbe.judging.dto.ScoreSubmissionDTO;
import com.fpt.swp.sealhackathonbe.judging.entity.*;
import com.fpt.swp.sealhackathonbe.judging.repository.*;
import com.fpt.swp.sealhackathonbe.judging.service.JudgingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JudgingServiceImpl implements JudgingService {

    private final JudgingRepository judgingRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final EventCriterionRepository eventCriterionRepository;
    private final EvaluationAuditLogRepository evaluationAuditLogRepository;

    @Autowired
    public JudgingServiceImpl(JudgingRepository judgingRepository,
                              UserRepository userRepository,
                              SubmissionRepository submissionRepository,
                              EventCriterionRepository eventCriterionRepository,
                              EvaluationAuditLogRepository evaluationAuditLogRepository) {
        this.judgingRepository = judgingRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.eventCriterionRepository = eventCriterionRepository;
        this.evaluationAuditLogRepository = evaluationAuditLogRepository;
    }

    @Override
    @Transactional
    public void recordScore(ScoreSubmissionDTO dto) {
        // 1. Fetch & validate that the submission exists
        Submission submission = submissionRepository.findById(dto.getSubmissionId())
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with ID: " + dto.getSubmissionId()));

        // 2. Fetch & validate that the judge exists
        User judge = userRepository.findById(dto.getJudgeUserId())
                .orElseThrow(() -> new EntityNotFoundException("Judge User not found with ID: " + dto.getJudgeUserId()));

        // 3. Fetch & validate that the actor (audit user) exists
        User actor = userRepository.findById(dto.getActorId())
                .orElseThrow(() -> new EntityNotFoundException("Actor User not found with ID: " + dto.getActorId()));

        // 4. Fetch & validate that the event criterion exists
        EventCriterion criterion = eventCriterionRepository.findById(dto.getEventCriterionId())
                .orElseThrow(() -> new EntityNotFoundException("Event Criterion not found with ID: " + dto.getEventCriterionId()));

        // 5. Validate that the score value does not exceed the maximum allowed value
        if (dto.getScoreValue().compareTo(criterion.getMaxScore()) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Score value %s exceeds the maximum allowed value %s for criterion '%s'.",
                    dto.getScoreValue(), criterion.getMaxScore(), criterion.getCriterionName()
            ));
        }

        // 6. Check if a score already exists for this submission, judge, and criterion
        Optional<Judging> existingScoreOpt = judgingRepository
                .findBySubmissionIdAndJudgeUserIdAndEventCriterionId(
                        dto.getSubmissionId(), dto.getJudgeUserId(), dto.getEventCriterionId()
                );

        String actionType;
        String oldValue = null;
        String newValue;
        Judging savedJudging;

        String formattedNewComment = dto.getComment() != null ? dto.getComment().replace("\"", "\\\"") : "";
        newValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", dto.getScoreValue(), formattedNewComment);

        if (existingScoreOpt.isPresent()) {
            Judging existingJudging = existingScoreOpt.get();
            actionType = "SCORE_UPDATE";
            
            String formattedOldComment = existingJudging.getComment() != null ? existingJudging.getComment().replace("\"", "\\\"") : "";
            oldValue = String.format("{\"score\":%s,\"comment\":\"%s\"}", existingJudging.getScoreValue(), formattedOldComment);

            existingJudging.setScoreValue(dto.getScoreValue());
            existingJudging.setComment(dto.getComment());
            if (dto.getIsCalibration() != null) {
                existingJudging.setIsCalibration(dto.getIsCalibration());
            }
            savedJudging = judgingRepository.save(existingJudging);
        } else {
            actionType = "SCORE_CREATE";
            
            Judging newJudging = new Judging();
            newJudging.setSubmission(submission);
            newJudging.setJudgeUser(judge);
            newJudging.setEventCriterion(criterion);
            newJudging.setScoreValue(dto.getScoreValue());
            newJudging.setComment(dto.getComment());
            newJudging.setIsCalibration(dto.getIsCalibration() != null ? dto.getIsCalibration() : false);
            
            savedJudging = judgingRepository.save(newJudging);
        }

        // 7. Extract Team and Event from the submission hierarchy
        Team team = submission.getTeam();
        Event event = (team != null) ? team.getEvent() : null;

        if (event == null) {
            throw new IllegalStateException("Could not log evaluation audit because the submission's event context is missing.");
        }

        // 8. Create and save the EvaluationAuditLog
        EvaluationAuditLog auditLog = new EvaluationAuditLog();
        auditLog.setEvent(event);
        auditLog.setActionType(actionType);
        auditLog.setActor(actor);
        auditLog.setScore(savedJudging);
        auditLog.setTeam(team);
        auditLog.setSubmission(submission);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setReason(dto.getReason());

        evaluationAuditLogRepository.save(auditLog);
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
    public List<JudgingDTO> getScoresByJudge(UUID judgeUserId) {
        return judgingRepository.findByJudgeUserId(judgeUserId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private JudgingDTO convertToDTO(Judging judging) {
        return JudgingDTO.builder()
                .id(judging.getId())
                .submissionId(judging.getSubmission() != null ? judging.getSubmission().getId() : null)
                .judgeUserId(judging.getJudgeUser() != null ? judging.getJudgeUser().getId() : null)
                .judgeName(judging.getJudgeUser() != null ? judging.getJudgeUser().getFullName() : null)
                .eventCriterionId(judging.getEventCriterion() != null ? judging.getEventCriterion().getId() : null)
                .criterionName(judging.getEventCriterion() != null ? judging.getEventCriterion().getCriterionName() : null)
                .scoreValue(judging.getScoreValue())
                .comment(judging.getComment())
                .scoredAt(judging.getScoredAt())
                .updatedAt(judging.getUpdatedAt())
                .isCalibration(judging.getIsCalibration())
                .build();
    }
}
