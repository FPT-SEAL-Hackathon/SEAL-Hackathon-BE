package com.fpt.swp.sealhackathonbe.submission.service;

import com.fpt.swp.sealhackathonbe.core.constant.SubmissionStatusConstants;
import com.fpt.swp.sealhackathonbe.submission.dto.DisqualifySubmissionRequest;
import com.fpt.swp.sealhackathonbe.submission.dto.SubmissionDisqualificationResponse;
import com.fpt.swp.sealhackathonbe.submission.entity.Submissions;
import com.fpt.swp.sealhackathonbe.submission.repository.SubmissionsRepository;
import com.fpt.swp.sealhackathonbe.submission.service.impl.SubmissionDisqualificationServiceImpl;
import com.fpt.swp.sealhackathonbe.team.entity.Disqualifications;
import com.fpt.swp.sealhackathonbe.team.repository.DisqualificationsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionDisqualificationServiceImplTest {

    @Mock
    private SubmissionsRepository submissionsRepository;

    @Mock
    private DisqualificationsRepository disqualificationsRepository;

    private SubmissionDisqualificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubmissionDisqualificationServiceImpl(
                submissionsRepository,
                disqualificationsRepository
        );
    }

    @Test
    void disqualifySubmissionUpdatesStatusAndWritesDisqualificationOnly() {
        UUID submissionId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();
        String reason = "Rule violation";
        Submissions submission = new Submissions();
        submission.setSubmissionId(submissionId);
        submission.setSubmissionStatusId(SubmissionStatusConstants.SUBMITTED);

        DisqualifySubmissionRequest request = new DisqualifySubmissionRequest();
        request.setReason(reason);

        when(submissionsRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(disqualificationsRepository.findBySubmissionId(submissionId)).thenReturn(List.of());
        when(disqualificationsRepository.save(any(Disqualifications.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubmissionDisqualificationResponse response =
                service.disqualifySubmission(submissionId, request, adminUserId);

        assertEquals(SubmissionStatusConstants.DISQUALIFIED, submission.getSubmissionStatusId());
        verify(submissionsRepository).save(submission);

        ArgumentCaptor<Disqualifications> disqualificationCaptor =
                ArgumentCaptor.forClass(Disqualifications.class);
        verify(disqualificationsRepository).save(disqualificationCaptor.capture());
        Disqualifications disqualification = disqualificationCaptor.getValue();
        assertNull(disqualification.getTeamId());
        assertEquals(submissionId, disqualification.getSubmissionId());
        assertEquals(reason, disqualification.getReason());
        assertEquals(adminUserId, disqualification.getDisqualifiedById());
        assertEquals(false, disqualification.getReversed());

        assertEquals(submissionId, response.getSubmissionId());
        assertEquals(reason, response.getReason());
    }
}
