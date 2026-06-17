package com.fpt.swp.sealhackathonbe.studentdownload.service.impl;

import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.studentdownload.dto.DownloadFileResponse;
import com.fpt.swp.sealhackathonbe.studentdownload.service.StudentDownloadService;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentDownloadServiceImpl implements StudentDownloadService {
    private static final String CSV_CONTENT_TYPE = "text/csv; charset=UTF-8";

    private final RoundRepository roundRepository;
    private final TeamMembersRepository teamMembersRepository;

    @Override
    @Transactional(readOnly = true)
    public DownloadFileResponse downloadRoundProblemCsv(UUID roundId, UUID currentUserId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));
        Category category = round.getCategory();
        Event event = category.getEvent();

        boolean canDownload = teamMembersRepository.existsActiveMemberInEventCategory(
                currentUserId,
                event.getEventId(),
                category.getCategoryId()
        );
        if (!canDownload) {
            throw new RuntimeException("You are not allowed to download this round problem");
        }

        byte[] content = writeCsv(
                new String[]{
                        "RoundID",
                        "EventName",
                        "CategoryName",
                        "RoundName",
                        "Description",
                        "RoundOrder",
                        "StartDate",
                        "EndDate",
                        "SubmissionDeadline",
                        "JudgingDeadline",
                        "AdvancementTopN"
                },
                new String[]{
                        valueOf(round.getRoundId()),
                        valueOf(event.getEventName()),
                        valueOf(category.getCategoryName()),
                        valueOf(round.getRoundName()),
                        valueOf(round.getDescription()),
                        valueOf(round.getRoundOrder()),
                        valueOf(round.getStartDate()),
                        valueOf(round.getEndDate()),
                        valueOf(round.getSubmissionDeadline()),
                        valueOf(round.getJudgingDeadline()),
                        valueOf(round.getAdvancementTopN())
                }
        );

        return new DownloadFileResponse(
                "round-problem-" + round.getRoundId() + ".csv",
                CSV_CONTENT_TYPE,
                content
        );
    }

    private byte[] writeCsv(String[] header, String[] row) {
        StringWriter writer = new StringWriter();
        writer.write('\uFEFF');

        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(header);
            csvWriter.writeNext(row);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV file", e);
        }

        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String valueOf(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
