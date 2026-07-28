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
    @NotBlank(message = "Event name is required")
    private String eventName;

    private String description;

    // Bat buoc: EventServiceImplementation.create() goi location.trim() -> thieu se NPE (500)
    // thay vi 400 co thong bao ro rang.
    @NotBlank(message = "Location is required")
    private String location;

    @URL(message = "Invalid banner image URL")
    private String bannerImageUrl;

    @FutureOrPresent(message = "Registration start time must be in the present or future")
    private LocalDateTime registrationStart;

    private LocalDateTime registrationEnd;

    @FutureOrPresent(message = "Event start time must be in the present or future")
    private LocalDate eventStartDate;

    private LocalDate eventEndDate;

    @NotNull(message = "Max team size is required")
    @Min(value = 1, message = "Max team size must be greater than 0")
    private Integer maxTeamSize;

    @NotNull(message = "Min team size is required")
    @Min(value = 1, message = "Min team size must be greater than 0")
    private Integer minTeamSize;

}
