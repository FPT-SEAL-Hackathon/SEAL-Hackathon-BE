package com.fpt.swp.sealhackathonbe.judge_assignment.service;

import com.fpt.swp.sealhackathonbe.judge_assignment.dto.AssignmentDTO;

import java.util.List;
import java.util.UUID;

public interface JudgeAssignmentService {
    List<AssignmentDTO> getJudgeAssignments(UUID judgeUserId);
}