package com.fpt.swp.sealhackathonbe.submission.service.impl;

import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifySubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifiedSubmissionResponse;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.SubmissionDisqualificationService;
import com.fpt.swp.sealhackathonbe.submission.service.mapper.SubmissionDisqualificationMapper;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmissionDisqualificationServiceImpl implements SubmissionDisqualificationService {
    private static final UUID SUBMISSION_STATUS_DISQUALIFIED =
            UUID.fromString("50000000-0000-0000-0000-000000000004");

    private final SubmissionsRepository submissionsRepository;
    private final DisqualificationsRepository disqualificationsRepository;

    @Override
    @Transactional
    public SubmissionDisqualificationResponse disqualifySubmission(
            UUID submissionId,
            DisqualifySubmissionRequest request,
            UUID adminUserId
    ) {
        // Luong nghiep vu:
        // 1. Admin chon mot submission bi sai rule/khong hop le.
        // 2. Kiem tra submission nay chua co disqualification active de tranh tao trung.
        // 3. Chi doi trang thai submission thanh DISQUALIFIED.
        // 4. Ghi Disqualifications voi SubmissionID co gia tri va TeamID = null.
        // Nhu vay code phan biet ro: day la loai bai nop, khong phai loai ca team.
        Submissions submission = submissionsRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        boolean alreadyDisqualified = disqualificationsRepository.findBySubmissionId(submissionId)
                .stream()
                .anyMatch(disqualification -> !Boolean.TRUE.equals(disqualification.getReversed()));

        if (alreadyDisqualified) {
            throw new RuntimeException("Submission is already disqualified");
        }

        submission.setSubmissionStatusId(SUBMISSION_STATUS_DISQUALIFIED);
        submission.setLastUpdatedAt(LocalDateTime.now());
        submissionsRepository.save(submission);

        Disqualifications disqualification = new Disqualifications();
        disqualification.setTeamId(null);
        disqualification.setSubmissionId(submissionId);
        disqualification.setReason(request.getReason());
        disqualification.setDisqualifiedById(adminUserId);
        disqualification.setDisqualifiedAt(LocalDateTime.now());
        disqualification.setReversed(false);

        Disqualifications saved = disqualificationsRepository.save(disqualification);
        return SubmissionDisqualificationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisqualifiedSubmissionResponse> getDisqualifiedSubmissions(UUID roundId) {
        // Luong du lieu: RoundID -> disqualification active -> submission lien quan -> DTO tong hop.
        return disqualificationsRepository
                .findActiveSubmissionDisqualifications(roundId)
                .stream()
                .map(SubmissionDisqualificationMapper::toDisqualifiedSubmissionResponse)
                .toList();
    }
}
