package com.fpt.swp.sealhackathonbe.event.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UpdateEventStatusRequest {
    private UUID eventStatusId;
}
