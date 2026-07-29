package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.submission.entity.SubmissionHistory;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubmissionHistoryService {
    private final SubmissionHistoryRepository submissionHistoryRepository;

    public SubmissionHistory recordSnapshot(Submissions submission) {
        int nextVersion = submissionHistoryRepository
                .findFirstBySubmissionIdOrderByVersionNumberDesc(submission.getSubmissionId())
                .map(history -> history.getVersionNumber() + 1)
                .orElse(1);

        SubmissionHistory history = new SubmissionHistory();
        history.setSubmissionId(submission.getSubmissionId());
        history.setVersionNumber(nextVersion);
        history.setTeamId(submission.getTeamId());
        history.setRoundId(submission.getRoundId());
        history.setSubmissionStatusId(submission.getSubmissionStatusId());
        history.setRepositoryUrl(submission.getRepositoryUrl());
        history.setDemoUrl(submission.getDemoUrl());
        history.setReportUrl(submission.getReportUrl());
        history.setSlideUrl(submission.getSlideUrl());
        history.setRepoMetadataJson(submission.getRepoMetadataJson());
        history.setRepoLastCommitAt(submission.getRepoLastCommitAt());
        history.setRepoStarCount(submission.getRepoStarCount());
        history.setRepoForkCount(submission.getRepoForkCount());
        history.setSubmittedAt(submission.getSubmittedAt());
        history.setLastUpdatedAt(submission.getLastUpdatedAt());
        history.setSubmittedByUserId(submission.getSubmittedByUserId());
        history.setNotes(submission.getNotes());
        history.setIsScoreApproved(Boolean.TRUE.equals(submission.getIsScoreApproved()));
        history.setIsSampleSubmission(Boolean.TRUE.equals(submission.getIsSampleSubmission()));
        history.setSnapshotCreatedAt(LocalDateTime.now());

        return submissionHistoryRepository.save(history);
    }
}
