package com.fpt.swp.sealhackathonbe.team.repository;

import com.fpt.swp.sealhackathonbe.team.entity.TeamWithdrawalRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamWithdrawalRequestRepository extends JpaRepository<TeamWithdrawalRequest, UUID> {

    @EntityGraph(attributePaths = {"team", "requestedBy"})
    List<TeamWithdrawalRequest> findByTeam_EventIdOrderByRequestedAtDesc(UUID eventId);

    @EntityGraph(attributePaths = {"team", "requestedBy"})
    List<TeamWithdrawalRequest> findByRequestedByIdOrderByRequestedAtDesc(UUID requestedById);
}
