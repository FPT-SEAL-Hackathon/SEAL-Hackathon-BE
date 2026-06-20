package com.fpt.swp.sealhackathonbe.certificate.service.impl;

import com.fpt.swp.sealhackathonbe.award.entity.Award;
import com.fpt.swp.sealhackathonbe.award.repository.AwardRepository;
import com.fpt.swp.sealhackathonbe.certificate.entity.Certificate;
import com.fpt.swp.sealhackathonbe.certificate.repository.CertificateRepository;
import com.fpt.swp.sealhackathonbe.certificate.service.CertificateService;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final AwardRepository awardRepository;
    private final CertificateRepository certificateRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TemplateEngine templateEngine;

    @Override
    @Transactional
    public byte[] generateCertificatePdf(UUID awardId, UUID currentUserId) {
        Award award = awardRepository.findByIdAndIsPublishedTrue(awardId)
                .orElseThrow(() -> new EntityNotFoundException("Published award not found"));

        boolean canDownload = teamMembersRepository
                .findByTeamIdAndUserIdAndActiveTrue(award.getTeam().getTeamId(), currentUserId)
                .isPresent();
        if (!canDownload) {
            throw new AccessDeniedException("You are not allowed to download this certificate");
        }

        Certificate certificate = certificateRepository.findByAwardId(awardId)
                .orElseGet(() -> createCertificate(award));

        Context context = new Context(Locale.forLanguageTag("vi-VN"));
        context.setVariable("teamName", award.getTeam().getTeamName());
        context.setVariable("awardTitle", award.getAwardTitle());
        context.setVariable("eventName", award.getEvent().getEventName());
        context.setVariable("certCode", certificate.getCertificateCode());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());
        context.setVariable("issuedDate", formatter.format(certificate.getGeneratedAt()));

        String htmlContent = templateEngine.process("certificate", context);
        return renderPdf(htmlContent);
    }

    private Certificate createCertificate(Award award) {
        Certificate certificate = new Certificate();
        certificate.setAward(award);
        certificate.setGeneratedAt(Instant.now());
        certificate.setCertificateCode("CERT-SEAL-" + award.getId().toString().toUpperCase());
        return certificateRepository.save(certificate);
    }

    private byte[] renderPdf(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "/");
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render certificate PDF", e);
        }
    }
}
