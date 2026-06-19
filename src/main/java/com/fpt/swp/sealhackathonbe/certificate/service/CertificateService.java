package com.fpt.swp.sealhackathonbe.certificate.service;

import java.util.UUID;

public interface CertificateService {
    byte[] generateCertificatePdf(UUID awardId, UUID currentUserId);
}
