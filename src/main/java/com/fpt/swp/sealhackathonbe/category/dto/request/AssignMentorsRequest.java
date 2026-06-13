package com.fpt.swp.sealhackathonbe.category.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AssignMentorsRequest {
    private List<UUID> mentorIds;
}
