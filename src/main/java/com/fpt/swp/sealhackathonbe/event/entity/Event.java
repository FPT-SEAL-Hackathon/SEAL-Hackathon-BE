package com.fpt.swp.sealhackathonbe.event.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @Column(name = "EventID")
    private UUID eventId;

    @Column(name = "EventName")
    private String eventName;

    @Column(name = "Description")
    private String description;

    @Column(name = "Location")
    private String location;

    @Column(name = "BannerImageURL")
    private String bannerImageUrl;

    @Column(name = "EventStatusID")
    private UUID eventStatusId;

    @Column(name = "RegistrationStart")
    private LocalDateTime registrationStart;

    @Column(name = "RegistrationEnd")
    private LocalDateTime registrationEnd;

    @Column(name = "EventStartDate")
    private LocalDate eventStartDate;

    @Column(name = "EventEndDate")
    private LocalDate eventEndDate;

    @Column(name = "MaxTeamSize")
    private Integer maxTeamSize;

    @Column(name = "MinTeamSize")
    private Integer minTeamSize;

    @Column(name = "CreatedByID")
    private UUID createdById;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

}
