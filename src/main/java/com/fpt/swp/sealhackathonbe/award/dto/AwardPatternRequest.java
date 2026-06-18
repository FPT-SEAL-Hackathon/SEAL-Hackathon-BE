package com.fpt.swp.sealhackathonbe.award.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AwardPatternRequest {

    @Valid
    @NotEmpty(message = "Award pattern list must not be empty")
    @Size(max = 10, message = "Only up to top 10 award patterns can be configured")
    private List<AwardPatternItemRequest> patterns;
}
