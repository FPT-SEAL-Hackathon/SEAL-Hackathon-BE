package com.fpt.swp.sealhackathonbe.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveUserRequest {

    @NotNull(message = "User type is required")
    private UUID userTypeId;
}
