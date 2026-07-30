package com.fpt.swp.sealhackathonbe.event.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateEventRequest {
    @NotBlank(message = "Event name must not be empty")
    private String eventName;

    private String description;

    private String location;

    @URL(message = "Invalid banner image URL")
    private String bannerImageUrl;

    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;

    private LocalDateTime eventStartDate;
    private LocalDateTime eventEndDate;

    @Min(value = 1, message = "Max team size must be greater than 0")
    private Integer maxTeamSize;

    @Min(value = 1, message = "Min team size must be greater than 0")
    private Integer minTeamSize;
}
