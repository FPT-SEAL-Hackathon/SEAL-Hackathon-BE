package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateMilestoneRequest {

    @NotBlank(message = "Label must not be blank")
    @Size(max = 255, message = "Label must not exceed 255 characters")
    private String label;

    private Integer sortOrder;
}
