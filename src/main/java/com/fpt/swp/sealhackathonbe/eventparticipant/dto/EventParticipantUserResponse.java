package com.fpt.swp.sealhackathonbe.eventparticipant.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EventParticipantUserResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    private String fptStudentCode;
    private String externalStudentCode;
    private String universityName;
    private String userTypeName;
    private String accountStatusName;
}
