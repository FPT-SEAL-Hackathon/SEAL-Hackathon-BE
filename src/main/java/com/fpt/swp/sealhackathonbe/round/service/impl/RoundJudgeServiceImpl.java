package com.fpt.swp.sealhackathonbe.round.service.impl;

import com.fpt.swp.sealhackathonbe.auth.dto.UserResponse;
import com.fpt.swp.sealhackathonbe.round.dto.request.AssignJudgesRequest;
import com.fpt.swp.sealhackathonbe.round.dto.response.JudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundJudgeResponse;
import com.fpt.swp.sealhackathonbe.round.dto.response.RoundResponse;
import com.fpt.swp.sealhackathonbe.round.entity.Round;
import com.fpt.swp.sealhackathonbe.round.entity.RoundJudge;
import com.fpt.swp.sealhackathonbe.round.repository.RoundJudgeRepository;
import com.fpt.swp.sealhackathonbe.round.repository.RoundRepository;
import com.fpt.swp.sealhackathonbe.round.service.RoundJudgeService;
import com.fpt.swp.sealhackathonbe.round.service.mapper.RoundMapper;
import com.fpt.swp.sealhackathonbe.user.entity.User;
import com.fpt.swp.sealhackathonbe.user.entity.UserType;
import com.fpt.swp.sealhackathonbe.user.repository.UserRepository;
import com.fpt.swp.sealhackathonbe.user.repository.UserTypeRepository;
import com.fpt.swp.sealhackathonbe.category.repository.CategoryMentorRepository;
import com.fpt.swp.sealhackathonbe.category.entity.CategoryMentor;
import com.fpt.swp.sealhackathonbe.judging.repository.JudgingRepository;
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
    private final UserTypeRepository userTypeRepository;
    private final RoundMapper roundMapper;
    private final CategoryMentorRepository categoryMentorRepository;
    private final JudgingRepository judgingRepository;

    public List<RoundJudgeResponse> assignJudges(UUID roundId, AssignJudgesRequest request) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
        List<User> judges = userRepository.findAllById(request.getUserIds());
        if (judges.isEmpty()) {
            throw new IllegalArgumentException("Judges not found");
        }

        // Get current user
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("Current user not found");
        }

        // BR-19: A Mentor can be a Judge in another Category, but must not judge the
        // same Category where they are assigned as Mentor.
        List<CategoryMentor> categoryMentors = categoryMentorRepository
                .findByCategory_CategoryId(round.getCategory().getCategoryId());
        for (User judge : judges) {
            boolean isMentorInCategory = categoryMentors.stream()
                    .anyMatch(cm -> cm.getMentor().getUserId().equals(judge.getUserId()));
            if (isMentorInCategory) {
                throw new IllegalArgumentException(
                        "Judge " + judge.getFullName() + " is already a mentor in this category");
            }
        }

        // Filter out judges already assigned to this round (prevent unique constraint violation)
        List<UUID> existingJudgeIds = roundJudgeRepository
                .findJudgesByRoundRoundId(roundId)
                .stream()
                .map(User::getUserId)
                .toList();
        List<User> newJudges = judges.stream()
                .filter(j -> !existingJudgeIds.contains(j.getUserId()))
                .toList();

        UserType expertType = userTypeRepository.findByTypeName("Expert")
                .orElseThrow(() -> new RuntimeException("Expert role not found"));

        for (User judge : newJudges) {
            String typeName = judge.getUserType().getTypeName();
            if (typeName.toLowerCase().contains("mentor")) {
                judge.setUserType(expertType);
                userRepository.save(judge);
            }
        }

        List<RoundJudge> roundJudges = newJudges
                .stream()
                .map(judge -> RoundJudge.builder()
                        .roundJudgeId(UUID.randomUUID())
                        .round(round)
                        .judge(judge)
                        .assignedAt(LocalDateTime.now())
                        .assignedBy(user)
                        .build())
                .toList();
        roundJudges = roundJudgeRepository.saveAll(roundJudges);
        return roundJudges.stream()
                .map(roundMapper::toRoundJudgeResponse)
                .toList();
    }

    @Override
    public List<RoundJudgeResponse> getJudgesByRound(UUID roundId) {
        if (!roundRepository.existsById(roundId)) {
            throw new EntityNotFoundException("Round not found");
        }

        return roundJudgeRepository.findByRoundRoundId(roundId)
                .stream()
                .map(roundMapper::toRoundJudgeResponse)
                .toList();
    }

    @Override
    public List<RoundResponse> getRoundsByJudge(UUID judgeId) {
        if (!userRepository.existsById(judgeId)) {
            throw new EntityNotFoundException("Judge not found");
        }
        return roundJudgeRepository.findRoundsByJudgeJudgeId(judgeId)
                .stream()
                .map(roundMapper::toRoundResponse)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void removeJudge(UUID roundJudgeId, boolean force) {
        RoundJudge roundJudge = roundJudgeRepository.findById(roundJudgeId)
                .orElseThrow(() -> new EntityNotFoundException("Round judge not found"));
        
        if (!force && judgingRepository.existsByRoundJudge_RoundJudgeId(roundJudgeId)) {
            throw new IllegalArgumentException("JUDGE_HAS_SCORES");
        }
        
        judgingRepository.deleteByRoundJudge_RoundJudgeId(roundJudgeId);
        roundJudgeRepository.delete(roundJudge);
    }

    @Override
    public List<UserResponse> getAllJudges() {
        return userRepository.findAll()
                .stream()
                .filter(user -> {
                    String type = user.getUserType().getTypeName();
                    return type.equalsIgnoreCase("Internal Judge")
                            || type.equalsIgnoreCase("Guest Judge");
                })
                .map(user -> UserResponse.builder()
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .toList();
    }
}
