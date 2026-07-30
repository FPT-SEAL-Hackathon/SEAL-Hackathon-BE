package com.fpt.swp.sealhackathonbe.certificate.service;

import com.fpt.swp.sealhackathonbe.certificate.dto.CertificateItemResponse;

import java.util.List;
import java.util.UUID;

public interface CertificateService {
    List<CertificateItemResponse> getCertificatesForUserInEvent(UUID eventId, UUID currentUserId);
    byte[] generateCertificatePdf(UUID awardId, UUID currentUserId);
    byte[] generateParticipationCertificatePdf(UUID eventId, UUID currentUserId);
}
