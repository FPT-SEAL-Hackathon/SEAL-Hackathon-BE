package com.fpt.swp.sealhackathonbe.award.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AwardPatternRequest {

    @Valid
    @NotEmpty(message = "Award pattern list must not be empty")
    private List<AwardPatternItemRequest> patterns;
}
