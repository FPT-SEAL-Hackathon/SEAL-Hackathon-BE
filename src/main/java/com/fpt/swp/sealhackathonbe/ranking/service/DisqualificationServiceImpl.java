//package com.fpt.swp.sealhackathonbe.ranking.service;
//
//import com.fpt.swp.sealhackathonbe.core.entity.User;
//import com.fpt.swp.sealhackathonbe.ranking.dto.DisqualificationDTO;
//import com.fpt.swp.sealhackathonbe.ranking.dto.DisqualificationRequestDTO;
//import com.fpt.swp.sealhackathonbe.ranking.entity.Disqualification;
//import com.fpt.swp.sealhackathonbe.ranking.repository.DisqualificationRepository;
//import com.fpt.swp.sealhackathonbe.submission.entity.Submission;
//import com.fpt.swp.sealhackathonbe.team.entity.Team;
//import jakarta.persistence.EntityManager;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//public class DisqualificationServiceImpl implements DisqualificationService {
//
//    private final DisqualificationRepository disqualificationRepository;
//    private final EntityManager entityManager;
//
//    public DisqualificationServiceImpl(DisqualificationRepository disqualificationRepository, EntityManager entityManager) {
//        this.disqualificationRepository = disqualificationRepository;
//        this.entityManager = entityManager;
//    }
//
//    @Override
//    @Transactional
//    public DisqualificationDTO disqualify(DisqualificationRequestDTO requestDTO, UUID disqualifiedById) {
//        Disqualification disqualification = new Disqualification();
//
//        if (requestDTO.getTeamId() != null) {
//            disqualification.setTeam(entityManager.getReference(Team.class, requestDTO.getTeamId()));
//        }
//        if (requestDTO.getSubmissionId() != null) {
//            disqualification.setSubmission(entityManager.getReference(Submission.class, requestDTO.getSubmissionId()));
//        }
//
//        disqualification.setReason(requestDTO.getReason());
//        disqualification.setDisqualifiedBy(entityManager.getReference(User.class, disqualifiedById));
//        disqualification.setIsReversed(false);
//
//        Disqualification saved = disqualificationRepository.save(disqualification);
//        return mapToDTO(saved);
//    }
//
//    @Override
//    @Transactional
//    public DisqualificationDTO reverseDisqualification(UUID disqualificationId, String reversalReason, UUID reversedById) {
//        Disqualification disqualification = disqualificationRepository.findById(disqualificationId)
//                .orElseThrow(() -> new IllegalArgumentException("Disqualification not found"));
//
//        disqualification.setIsReversed(true);
//        disqualification.setReversalReason(reversalReason);
//        disqualification.setReversedBy(entityManager.getReference(User.class, reversedById));
//        disqualification.setReversedAt(LocalDateTime.now());
//
//        Disqualification updated = disqualificationRepository.save(disqualification);
//        return mapToDTO(updated);
//    }
//
//    @Override
//    public List<DisqualificationDTO> getDisqualificationsByTeam(UUID teamId) {
//        return disqualificationRepository.findByTeamId(teamId).stream()
//                .map(this::mapToDTO)
//                .collect(Collectors.toList());
//    }
//
//    private DisqualificationDTO mapToDTO(Disqualification d) {
//        return DisqualificationDTO.builder()
//                .id(d.getId())
//                .teamId(d.getTeam() != null ? d.getTeam().getId() : null)
//                .submissionId(d.getSubmission() != null ? d.getSubmission().getId() : null)
//                .reason(d.getReason())
//                .disqualifiedById(d.getDisqualifiedBy() != null ? d.getDisqualifiedBy().getId() : null)
//                .disqualifiedAt(d.getDisqualifiedAt())
//                .isReversed(d.getIsReversed())
//                .reversedAt(d.getReversedAt())
//                .reversedById(d.getReversedBy() != null ? d.getReversedBy().getId() : null)
//                .reversalReason(d.getReversalReason())
//                .build();
//    }
//}
