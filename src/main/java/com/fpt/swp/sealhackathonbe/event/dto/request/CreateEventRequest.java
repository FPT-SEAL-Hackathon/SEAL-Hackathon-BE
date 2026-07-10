package com.fpt.swp.sealhackathonbe.event.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateEventRequest {
    @NotBlank(message = "Event name must not be empty")
    private String eventName;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @URL(message = "Invalid banner image URL")
    private String bannerImageUrl;

    @NotNull(message = "Event status is required")
    private UUID eventStatusId;

    @NotNull(message = "Registration start time is required")
    @FutureOrPresent(message = "Registration start time must be in the present or future")
    private LocalDateTime registrationStart;

    @NotNull(message = "Registration end time is required")
    private LocalDateTime registrationEnd;

    @NotNull(message = "Event start date is required")
    private LocalDate eventStartDate;

    @NotNull(message = "Event end date is required")
    private LocalDate eventEndDate;

    @NotNull(message = "Max team size is required")
    @Min(value = 1, message = "Max team size must be greater than 0")
    private Integer maxTeamSize;

    @NotNull(message = "Min team size is required")
    @Min(value = 1, message = "Min team size must be greater than 0")
    private Integer minTeamSize;

}
