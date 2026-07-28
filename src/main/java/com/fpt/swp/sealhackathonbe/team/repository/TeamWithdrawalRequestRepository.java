package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamWithdrawalRequestRepository extends JpaRepository<TeamWithdrawalRequest, UUID> {

    boolean existsByTeamIdAndRequestStatus(UUID teamId, String requestStatus);

    @EntityGraph(attributePaths = {"team", "requestedBy"})
    List<TeamWithdrawalRequest> findByTeam_EventIdAndRequestStatus(UUID eventId, String requestStatus);

    @EntityGraph(attributePaths = {"team", "requestedBy"})
    List<TeamWithdrawalRequest> findByRequestedByIdAndRequestStatus(UUID requestedById, String requestStatus);

    @EntityGraph(attributePaths = {"team", "requestedBy"})
    Optional<TeamWithdrawalRequest> findByRequestIdAndRequestStatus(UUID requestId, String requestStatus);
}
