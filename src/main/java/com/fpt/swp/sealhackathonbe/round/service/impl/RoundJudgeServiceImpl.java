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
import com.fpt.swp.sealhackathonbe.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;

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
    private final NotificationService notificationService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public List<RoundJudgeResponse> assignJudges(UUID roundId, AssignJudgesRequest request) {
        Round round = roundRepository
                .findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("Round not found"));
        List<User> judges = userRepository.findAllById(request.getJudgeIds());
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
        List<RoundJudge> allExisting = roundJudgeRepository.findByRoundRoundId(roundId);
        List<RoundJudge> finalRoundJudges = new java.util.ArrayList<>();
        for (RoundJudge newRj : roundJudges) {
            java.util.Optional<RoundJudge> existing = allExisting.stream()
                    .filter(r -> r.getJudge().getUserId().equals(newRj.getJudge().getUserId()))
                    .findFirst();
            if (existing.isPresent()) {
                RoundJudge rjToUpdate = existing.get();
                rjToUpdate.setIsActive(true);
                rjToUpdate.setAssignedAt(LocalDateTime.now());
                rjToUpdate.setAssignedBy(user);
                finalRoundJudges.add(roundJudgeRepository.save(rjToUpdate));
            } else {
                newRj.setIsActive(true);
                finalRoundJudges.add(roundJudgeRepository.save(newRj));
            }
        }
        roundJudges = finalRoundJudges;

        for (RoundJudge rj : roundJudges) {
            try {
                com.fpt.swp.sealhackathonbe.event.entity.Event event = round.getCategory().getEvent();
                String title = "New Judge Assignment";
                String body = String.format("You have been assigned as a Judge for Round: %s in Category: %s, Event: %s.\n" +
                                "Event Date: %s to %s\n" +
                                "Event Link: %s/events/%s",
                        round.getRoundName(),
                        round.getCategory().getCategoryName(),
                        event.getEventName(),
                        event.getEventStartDate(),
                        event.getEventEndDate(),
                        frontendUrl,
                        event.getEventId());
                notificationService.sendNotification(rj.getJudge().getUserId(), user.getUserId(), event.getEventId(), title, body);
            } catch (Exception e) {
                System.err.println("Failed to send notification: " + e.getMessage());
            }
        }

        return roundJudges.stream()
                .map(roundMapper::toRoundJudgeResponse)
                .toList();
    }

    @Override
    public List<RoundJudgeResponse> getJudgesByRound(UUID roundId) {
        if (!roundRepository.existsById(roundId)) {
            throw new EntityNotFoundException("Round not found");
        }

        return roundJudgeRepository.findActiveByRoundRoundId(roundId)
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
        
        // Conditional deletion is handled below
        if (force) {
            judgingRepository.disableByRoundJudge_RoundJudgeId(roundJudgeId);
        } else {
            // Keep judging records if not forced
        }
        roundJudge.setIsActive(false);
        roundJudgeRepository.save(roundJudge);
    }

    @Override
    public List<JudgeResponse> getAllJudges() {
        return userRepository.findExpertsMentorsJudges()
                .stream()
                .map(user -> JudgeResponse.builder()
                        .judgeId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(toApiName(getRoleName(user)))
                        .roleName(getRoleName(user))
                        .build())
                .toList();
    }

    private String getRoleName(User user) {
        return user.getUserType() != null ? user.getUserType().getTypeName() : null;
    }

    private String toApiName(String value) {
        return value == null ? null : value.trim().replace(' ', '_').toUpperCase();
    }
}
