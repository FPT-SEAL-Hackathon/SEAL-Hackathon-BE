package com.fpt.swp.sealhackathonbe.certificate.entity;

import com.fpt.swp.sealhackathonbe.award.entity.Award;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "Certificates")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CertificateID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AwardID", nullable = false, unique = true)
    private Award award;

    @Column(name = "CertificateCode", nullable = false, unique = true, length = 100)
    private String certificateCode;

    @Column(name = "GeneratedAt", nullable = false)
    private Instant generatedAt;
}
