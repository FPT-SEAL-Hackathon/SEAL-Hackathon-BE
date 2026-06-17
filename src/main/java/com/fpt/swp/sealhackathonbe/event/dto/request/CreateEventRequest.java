package com.fpt.swp.sealhackathonbe.event.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fpt.swp.sealhackathonbe.event.entity.EventStatus;
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

    private String location;

    @URL(message = "Invalid banner image URL")
    private String bannerImageUrl;

    @NotNull(message = "Event status is required")
    private UUID eventStatusId;

    @FutureOrPresent(message = "Registration start time must be in the present or future")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEnd;

    private LocalDate eventStartDate;
    private LocalDate eventEndDate;

    @Min(value = 1, message = "Max team size must be greater than 0")
    private Integer maxTeamSize;

    @Min(value = 1, message = "Min team size must be greater than 0")
    private Integer minTeamSize;

}
