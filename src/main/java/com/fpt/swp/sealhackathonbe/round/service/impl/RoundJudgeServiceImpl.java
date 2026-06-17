package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.round.dto.request.AssignJudgesRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import com.fpt.swp.sealhackathonbe.round.service.mapper.RoundMapper;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundJudgeServiceImpl implements RoundJudgeService {
    private final RoundRepository roundRepository;
    private final RoundJudgeRepository roundJudgeRepository;
    private final UserRepository userRepository;
    private final RoundMapper roundMapper;

    public List<RoundJudgeResponse> assignJudges(UUID roundId, AssignJudgesRequest request) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
        List<User> judges = userRepository.findAllById(request.getUserIds());
        if (judges.isEmpty()) {
            throw new IllegalArgumentException("Judges not found");
        }

        //Get current user
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("Current user not found");
        }

        List<RoundJudge> roundJudges = judges
                .stream()
                .map(judge -> RoundJudge.builder()
                        .roundJudgeId(UUID.randomUUID())
                        .round(round)
                        .judge(judge)
                        .assignedAt(LocalDateTime.now())
                        .assignedBy(user)
                        .build()
                )
                .toList();
        roundJudges = roundJudgeRepository.saveAll(roundJudges);
        return roundJudges.stream()
                .map(roundMapper::toRoundJudgeResponse)
                .toList();
    }

    @Override
    public List<JudgeResponse> getJudgesByRound(UUID roundId) {
        if (!roundRepository.existsById(roundId)) {
            throw new EntityNotFoundException("Round not found");
        }

        return roundJudgeRepository.findJudgesByRoundRoundId(roundId)
                .stream()
                .map(roundMapper::toJudgeResponse)
                .toList();
    }

    @Override
    public void removeJudge(UUID roundJudgeId) {
        RoundJudge roundJudge = roundJudgeRepository.findById(roundJudgeId)
                .orElseThrow(() -> new EntityNotFoundException("Round judge not found"));
        //Add constraints before delete later
        roundJudgeRepository.delete(roundJudge);
    }
}
