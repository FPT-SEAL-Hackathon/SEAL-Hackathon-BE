package com.fpt.swp.sealhackathonbe.certificate.service.impl;

import com.fpt.swp.sealhackathonbe.award.entity.Award;
import com.fpt.swp.sealhackathonbe.award.repository.AwardRepository;
import com.fpt.swp.sealhackathonbe.category.entity.Category;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryRepository;
import com.fpt.swp.sealhackathonbe.certificate.dto.CertificateItemResponse;
import com.fpt.swp.sealhackathonbe.certificate.entity.Certificate;
import com.fpt.swp.sealhackathonbe.certificate.repository.CertificateRepository;
import com.fpt.swp.sealhackathonbe.certificate.service.CertificateService;
import com.fpt.swp.sealhackathonbe.event.entity.Event;
import com.fpt.swp.sealhackathonbe.event.repository.EventRepository;
import com.fpt.swp.sealhackathonbe.team.entity.TeamMembers;
import com.fpt.swp.sealhackathonbe.team.entity.Teams;
import com.fpt.swp.sealhackathonbe.team.repository.TeamMembersRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final AwardRepository awardRepository;
    private final CertificateRepository certificateRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final TemplateEngine templateEngine;

    @Override
    @Transactional(readOnly = true)
    public List<CertificateItemResponse> getCertificatesForUserInEvent(UUID eventId, UUID currentUserId) {
        TeamMembers member = teamMembersRepository.findActiveMemberInEvent(eventId, currentUserId)
                .orElse(null);
        if (member == null) {
            return List.of();
        }

        Teams team = member.getTeam();
        List<CertificateItemResponse> result = new ArrayList<>();

        Event event = eventRepository.findById(eventId).orElse(null);
        String eventName = event != null ? event.getEventName() : (team.getEvent() != null ? team.getEvent().getEventName() : "Hackathon Event");

        String categoryName = null;
        if (team.getCategory() != null) {
            categoryName = team.getCategory().getCategoryName();
        } else if (team.getCategoryId() != null) {
            Category cat = categoryRepository.findById(team.getCategoryId()).orElse(null);
            if (cat != null) categoryName = cat.getCategoryName();
        }

        // 1. Participation Certificate
        Instant participationDate = team.getCreatedAt() != null
                ? team.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                : Instant.now();

        result.add(CertificateItemResponse.builder()
                .id("participation_" + eventId)
                .type("PARTICIPATION")
                .awardId(null)
                .eventId(eventId)
                .eventName(eventName)
                .categoryId(team.getCategoryId())
                .categoryName(categoryName)
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .title("Certificate of Participation")
                .awardTierName("PARTICIPANT")
                .publishedAt(participationDate)
                .build());

        // 2. Award Certificates (if team won any published awards)
        List<Award> awards = awardRepository.findByEventEventIdAndTeamTeamIdAndIsPublishedTrue(eventId, team.getTeamId());
        for (Award award : awards) {
            String awardCatName = award.getCategory() != null ? award.getCategory().getCategoryName() : categoryName;
            result.add(CertificateItemResponse.builder()
                    .id(award.getId().toString())
                    .type("AWARD")
                    .awardId(award.getId())
                    .eventId(eventId)
                    .eventName(award.getEvent() != null ? award.getEvent().getEventName() : eventName)
                    .categoryId(award.getCategory() != null ? award.getCategory().getCategoryId() : team.getCategoryId())
                    .categoryName(awardCatName)
                    .teamId(team.getTeamId())
                    .teamName(team.getTeamName())
                    .title(award.getAwardTitle())
                    .awardTierName(award.getAwardTier() != null ? award.getAwardTier().getTierName() : null)
                    .publishedAt(award.getPublishedAt() != null ? award.getPublishedAt() : award.getAwardedAt())
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateParticipationCertificatePdf(UUID eventId, UUID currentUserId) {
        TeamMembers member = teamMembersRepository.findActiveMemberInEvent(eventId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("You are not an active team member in this event"));

        Teams team = member.getTeam();

        Event event = eventRepository.findById(eventId).orElse(null);
        String eventName = event != null ? event.getEventName() : (team.getEvent() != null ? team.getEvent().getEventName() : "");

        String categoryName = "";
        if (team.getCategory() != null) {
            categoryName = team.getCategory().getCategoryName();
        } else if (team.getCategoryId() != null) {
            Category cat = categoryRepository.findById(team.getCategoryId()).orElse(null);
            if (cat != null) categoryName = cat.getCategoryName();
        }

        Context context = new Context(Locale.forLanguageTag("vi-VN"));
        context.setVariable("teamName", team.getTeamName() != null ? team.getTeamName() : "Team");
        context.setVariable("awardTitle", "ACTIVE PARTICIPANT");
        context.setVariable("eventName", eventName);
        context.setVariable("categoryName", categoryName);
        context.setVariable("awardTierName", "CERTIFIED PARTICIPANT");
        context.setVariable("certMainTitle", "Certificate of Participation");
        context.setVariable("certSubtitle", "This certificate is proudly presented to");
        context.setVariable("citation", "in recognition of active participation and valuable contribution in");

        String certCode = "CERT-SEAL-PART-" + eventId.toString().substring(0, 8).toUpperCase() + "-" + team.getTeamId().toString().substring(0, 8).toUpperCase();
        context.setVariable("certCode", certCode);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());
        context.setVariable("issuedDate", formatter.format(Instant.now()));

        context.setVariable("brandLogoBase64", getLogoBase64WithAlpha(1.0f));
        context.setVariable("watermarkLogoBase64", getLogoBase64WithAlpha(0.30f));

        String htmlContent = templateEngine.process("certificate", context);
        return renderPdf(htmlContent);
    }

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
        context.setVariable("teamName", award.getTeam() != null ? award.getTeam().getTeamName() : "Team");
        context.setVariable("awardTitle", award.getAwardTitle() != null ? award.getAwardTitle() : "Award");
        context.setVariable("eventName", award.getEvent() != null ? award.getEvent().getEventName() : "");

        String categoryName = "";
        if (award.getCategory() != null) {
            categoryName = award.getCategory().getCategoryName();
        }
        context.setVariable("categoryName", categoryName);

        String awardTierName = "";
        if (award.getAwardTier() != null) {
            awardTierName = award.getAwardTier().getTierName();
        }
        context.setVariable("awardTierName", awardTierName);

        context.setVariable("certMainTitle", "Certificate of Excellence");
        context.setVariable("certSubtitle", "This certificate is proudly presented to");
        context.setVariable("citation", "for outstanding performance and successfully winning the award");
        context.setVariable("certCode", certificate.getCertificateCode());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());
        context.setVariable("issuedDate", formatter.format(certificate.getGeneratedAt()));

        context.setVariable("brandLogoBase64", getLogoBase64WithAlpha(1.0f));
        context.setVariable("watermarkLogoBase64", getLogoBase64WithAlpha(0.30f));

        String htmlContent = templateEngine.process("certificate", context);
        return renderPdf(htmlContent);
    }

    private String getLogoBase64WithAlpha(float alpha) {
        try (InputStream is = getClass().getResourceAsStream("/static/logo_trans.png")) {
            if (is != null) {
                byte[] rawBytes = is.readAllBytes();
                if (alpha >= 0.99f) {
                    return "data:image/png;base64," + Base64.getEncoder().encodeToString(rawBytes);
                }
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawBytes));
                if (original != null) {
                    BufferedImage transparentImage = new BufferedImage(
                            original.getWidth(),
                            original.getHeight(),
                            BufferedImage.TYPE_INT_ARGB
                    );
                    Graphics2D g2d = transparentImage.createGraphics();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2d.drawImage(original, 0, 0, null);
                    g2d.dispose();

                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(transparentImage, "png", os);
                    return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate transparent logo base64 with alpha " + alpha, e);
        }
        return "";
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
            log.error("Failed to render certificate PDF: ", e);
            throw new RuntimeException("Failed to render certificate PDF", e);
        }
    }
}
