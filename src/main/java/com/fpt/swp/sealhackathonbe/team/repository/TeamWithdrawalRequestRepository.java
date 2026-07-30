package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamWithdrawalRequestRepository extends JpaRepository<TeamWithdrawalRequest, UUID> {

    // Fetch team/requestedBy de service map response list khong bi N+1.
    @EntityGraph(attributePaths = {"team", "requestedBy"})
    List<TeamWithdrawalRequest> findByTeam_EventIdOrderByRequestedAtDesc(UUID eventId);

    // Lich su withdrawal cua mot user, sap xep moi nhat truoc cho man "mine".
    @EntityGraph(attributePaths = {"team", "requestedBy"})
    List<TeamWithdrawalRequest> findByRequestedByIdOrderByRequestedAtDesc(UUID requestedById);

    // Lay request gan nhat khi can enrich lifecycle detail cua team.
    @EntityGraph(attributePaths = {"requestedBy"})
    Optional<TeamWithdrawalRequest> findTopByTeamIdOrderByRequestedAtDesc(UUID teamId);
}
